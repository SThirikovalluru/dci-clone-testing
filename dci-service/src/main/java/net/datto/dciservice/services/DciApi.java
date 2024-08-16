package net.datto.dciservice.services;

import net.datto.dciservice.config.DciConfig;
import net.datto.dciservice.dynamodb.AccountMapping;
import net.datto.dciservice.utils.AESCodec;
import net.datto.dciservice.utils.InternalServerErrorException;
import org.apache.hc.client5.http.utils.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.UUID;

public abstract class DciApi {
    private static final String DEVICES_PATH = "/v1/bcdr/device";
    private static final String AGENTS_PATH = "/asset/agent";
    private static final String SAAS_PATH = "/v1/saas/domains";

    @Autowired
    private DciConfig config;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AESCodec aesCodec;

    private final String getPortalAuthorizationHeader(String publicKey, String privateKey) {
        String plainCredentials = publicKey + ":" + privateKey;
        String base64Credentials = new String(Base64.encodeBase64(plainCredentials.getBytes()));
        return base64Credentials;
    }

    private final HttpEntity<String> createPortalRequestEntity(String endpoint, HttpMethod method, String payload, AccountMapping accountMapping) throws InternalServerErrorException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

        String authorization = getPortalAuthorizationHeader(accountMapping.getPortalPublicKey(), aesCodec.decode(accountMapping.getPortalSecretKey()));
        headers.add("Authorization", "Basic " + authorization);
        headers.add("X-Request-Id", "DattoRMM" + '-' + config.getPlatform() +'-' + accountMapping.getRmmAccountUid() +'-' + UUID.randomUUID().toString());
        if ((method == HttpMethod.POST || method == HttpMethod.PUT || method == HttpMethod.DELETE) && payload != null && !payload.isEmpty()) {
            return new HttpEntity<>(payload, headers);
        } else {
            return new HttpEntity<>(headers);
        }
    }

    protected <T> ResponseEntity<T> requestPortal(AccountMapping accountMapping, HttpMethod method, String endpoint, ParameterizedTypeReference<T> responseTypeReference) throws RestClientException, InternalServerErrorException {
        HttpEntity<String> requestEntity = createPortalRequestEntity(endpoint, method, null, accountMapping);
        return restTemplate.exchange(endpoint, method, requestEntity, responseTypeReference);
    }

    protected String getAgentsEndpoint(String serialNumber, String page, String size) {
        String pagination = "?_page=" + page + "&_perPage=" + size;
        return config.getPortalApiUrl() + DEVICES_PATH + "/" + serialNumber + AGENTS_PATH + pagination;
    }

    protected String getDeviceEndpoint(String serialNumber) {
        return config.getPortalApiUrl() + DEVICES_PATH + "/" + serialNumber;
    }

    protected String getDevicesEndpoint(String page, String size) {
        String pagination = "?_page=" + page + "&_perPage=" + size;
        return config.getPortalApiUrl() + DEVICES_PATH + pagination;
    }

    protected String getSaasEndpoint() {
        return config.getPortalApiUrl() + SAAS_PATH;
    }

}
