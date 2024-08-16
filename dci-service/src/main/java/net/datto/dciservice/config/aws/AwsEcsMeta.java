package net.datto.dciservice.config.aws;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
public class AwsEcsMeta {

    /**
     * Retrieve the ECS Meta Data in JSON format.
     *
     * @return The ECS Meta data, if available or <code>null</code>
     */
    public String getEcsTaskMeta() {
        String ecsMetaUri = System.getenv("ECS_CONTAINER_METADATA_URI");
        if (ecsMetaUri != null) {
            try {
                return getEcsTaskMetaFromAws(ecsMetaUri);
            } catch (Exception e) {
                // lets try again, sometimes the service does not appear to be available immediately
                log.info("Unable to retrieve the ecs meta data at {} trying again", ecsMetaUri);
                try {
                    return getEcsTaskMetaFromAws(ecsMetaUri);
                } catch (Exception e2) {
                    log.warn("Unable to retrieve the ecs meta data at {} trying again", ecsMetaUri, e2);
                }
            }
        }
        return null;
    }

    private String getEcsTaskMetaFromAws(String ecsMetaUri) throws RestClientException {
        RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());
        ResponseEntity<String> response = restTemplate.getForEntity(ecsMetaUri + "/task", String.class);
        return response.getBody();
    }

    private ClientHttpRequestFactory getClientHttpRequestFactory() {
        var timeout = Timeout.of(20, TimeUnit.SECONDS);
        var requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(timeout)
                .build();
        var httpClient = HttpClientBuilder
                .create()
                .setDefaultRequestConfig(requestConfig)
                .build();
        return new HttpComponentsClientHttpRequestFactory(httpClient);
    }
}
