package net.datto.dciservice.services;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.datto.dci.api.dto.DattoContinuityDevice;
import net.datto.dci.api.dto.DeviceResponse;
import net.datto.dci.api.dto.Pagination;
import net.datto.dci.api.dto.agent.AgentDto;
import net.datto.dciservice.configuration.SpringTestConfig;
import net.datto.dciservice.config.DciConfig;
import net.datto.dciservice.dynamodb.AccountMapping;
import net.datto.dciservice.utils.AESCodec;
import net.datto.dciservice.utils.BadRequestException;
import net.datto.dciservice.utils.DCIDevice;
import net.datto.dciservice.utils.InternalServerErrorException;
import net.datto.dciservice.utils.NotFoundException;
import net.datto.dciservice.utils.UnauthorizedException;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@SpringBootTest
@Import(SpringTestConfig.class)
public class DciServiceTest {

    @InjectMocks
    private DciService dciService;
    @Mock
    private AESCodec aesCodec;
    @Mock
    private DciConfig config;
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private ObjectMapper objectMapper;

    @Autowired
    private ObjectMapper mapper;

    @Test
    public void getDeviceAgents_Should_Return_DeviceResponse_With_AgentDto() throws URISyntaxException, UnauthorizedException, InternalServerErrorException, BadRequestException, NotFoundException {
        String privateKey = "zhMN7YA0XZluDpAn8IRwBA==;PO4TOponKUYMTyL1l+7gXA==";
        AccountMapping accountMapping = new AccountMapping("accountId", "publicKey", privateKey);

        when(aesCodec.decode(any(String.class))).thenReturn("foo");
        when(config.getPortalApiUrl()).thenReturn("");

        Pagination pagination = new Pagination(1, 50, 1, 1);
        AgentDto agentDto = new AgentDto("Clint-PC", "6dc44aad3437402ebab1ece3ae8ce772", "192.168.0.18", 1591632007L, 1591632007L, "true", 0L, 1591632007L, "2.4.3.0", 2, 0, true, false, List.of("E:\\", "C:\\"), new ArrayList<>(), null);

        DeviceResponse<AgentDto> expected = new DeviceResponse<>(pagination, List.of(agentDto));

        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class))).thenReturn(ResponseEntity.ok(expected));

        DeviceResponse<AgentDto> actual = dciService.getDeviceAgents(accountMapping, "serialNumber1", "1", "50");

        assertEquals(expected, actual);
    }

    @Test
    public void getDciDevices_Should_Return_DeviceResponse_With_DattoContinuityDevice() throws UnauthorizedException, InternalServerErrorException, BadRequestException, NotFoundException, JsonProcessingException {
        String privateKey = "zhMN7YA0XZluDpAn8IRwBA==;PO4TOponKUYMTyL1l+7gXA==";
        AccountMapping accountMapping = new AccountMapping("accountId", "publicKey", privateKey);

        when(aesCodec.decode(any(String.class))).thenReturn("foo");
        when(config.getPortalApiUrl()).thenReturn("");

        Pagination pagination = new Pagination(1, 50, 1, 1);

        DCIDevice dciDevice = new DCIDevice();
        dciDevice.setSerialNumber("serialNumber1");
        dciDevice.setName("DeviceName");
        dciDevice.setModel("DeviceModel");
        dciDevice.setInternalIpAddress("192.168.0.18");
        dciDevice.setClientCompanyName("ClientCompanyName");

        DattoContinuityDevice dattoContinuityDevice = new DattoContinuityDevice();
        dattoContinuityDevice.setSerialNumber("serialNumber1");
        dattoContinuityDevice.setName("DeviceName");
        dattoContinuityDevice.setModel("DeviceModel");
        dattoContinuityDevice.setInternalIpAddress("192.168.0.18");
        dattoContinuityDevice.setClientCompanyName("ClientCompanyName");

        DeviceResponse<DCIDevice> restTemplateExpectedResult = new DeviceResponse<>(pagination, List.of(dciDevice));
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class))).thenReturn(ResponseEntity.ok(restTemplateExpectedResult));

        String restTemplateExpectedResultAsString = mapper.writeValueAsString(restTemplateExpectedResult);
        when(objectMapper.writeValueAsString(any(DeviceResponse.class))).thenReturn(restTemplateExpectedResultAsString);

        DeviceResponse<DattoContinuityDevice> expectedDeviceResponse = mapper.readValue(mapper.writeValueAsString(restTemplateExpectedResult), new TypeReference<DeviceResponse<DattoContinuityDevice>>() {
        });
        when(objectMapper.readValue(any(String.class), any(TypeReference.class))).thenReturn(expectedDeviceResponse);

        DeviceResponse<DattoContinuityDevice> devices = dciService.getDciDevices(accountMapping, "1", "50");
        assertEquals(expectedDeviceResponse, devices);

    }
}