package net.datto.dciservice.controllers;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.datto.dci.api.dto.DattoContinuityDevice;
import net.datto.dci.api.dto.DeviceResponse;
import net.datto.dci.api.dto.Pagination;
import net.datto.dci.api.dto.agent.AgentDto;
import net.datto.dciservice.configuration.SpringTestConfig;
import net.datto.dciservice.dynamodb.AccountMapping;
import net.datto.dciservice.dynamodb.DeviceMapping;
import net.datto.dciservice.dynamodb.SiteMapping;
import net.datto.dciservice.services.AccountMappingProcessor;
import net.datto.dciservice.services.DciService;
import net.datto.dciservice.services.SiteMappingProcessor;
import net.datto.dciservice.utils.ExceptionController;
import net.datto.dciservice.utils.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.LinkedList;

import static net.datto.dciservice.utils.JsonParser.convertToString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;


@SpringBootTest
@Import(SpringTestConfig.class)
public class DeviceControllerTest {

    @Autowired
    DeviceController deviceController;
    @MockBean
    AccountMappingProcessor accountMappingProcessor;
    @MockBean
    SiteMappingProcessor siteMappingProcessor;
    @MockBean
    DciService dciService;

    private MockMvc mockMvc;

    AccountMapping accountMapping;
    DeviceMapping deviceMapping;

    DattoContinuityDevice dattoContinuityDevice;
    LinkedList<SiteMapping> siteMappingList;
    DeviceResponse<DattoContinuityDevice> deviceResponseDevice;
    DeviceResponse<AgentDto> deviceResponseAgent;
    Pagination pagination;
    ArrayList<AgentDto> agents;
    AgentDto agent;
    NotFoundException exception;

    @Autowired
    ObjectMapper mapper;

    @BeforeEach
    public void setup() {

        this.mockMvc = standaloneSetup(this.deviceController).setControllerAdvice(new ExceptionController()).build();

        accountMapping = new AccountMapping("rmmAccountUid", "publicKey", "privateKey");
        dattoContinuityDevice = new DattoContinuityDevice();
        dattoContinuityDevice.setSerialNumber("testSerialNumber");
        dattoContinuityDevice.setName("testName");
        dattoContinuityDevice.setModel("testModel");
        dattoContinuityDevice.setInternalIpAddress("testInternalIpAddress");
        dattoContinuityDevice.setClientCompanyName("testClientCompanyName");
        dattoContinuityDevice.setRmmSiteUid("testRmmSiteUid");
        deviceResponseDevice = new DeviceResponse<>();
        deviceResponseDevice.setItems(new ArrayList<>() {{
            add(dattoContinuityDevice);
        }});
        pagination = new Pagination(1, 1, 1, 1);
        deviceResponseDevice.setPagination(pagination);
        siteMappingList = new LinkedList<>();
        agents = new ArrayList<>();
        agent = new AgentDto();
        agent.setName("name");
        agent.setLocalIp("localIp");
        agents.add(agent);
        deviceMapping = new DeviceMapping();
        deviceMapping.setDciDeviceUid("test");
        deviceMapping.setProtectedDeviceUid("test");
        deviceMapping.setSerialNumber("test");

        deviceResponseAgent = new DeviceResponse<>();
        deviceResponseAgent.setItems(new ArrayList<>() {{
            add(agent);
        }});
        deviceResponseAgent.setPagination(pagination);

        exception = new NotFoundException("Test NotFoundException");
    }

    @Test
    public void getAllDattoContinuityDeviceTest() throws Exception {
        when(accountMappingProcessor.checkAccountMapping(any(String.class))).thenReturn(accountMapping);
        when(siteMappingProcessor.getSiteMappings(any(String.class))).thenReturn(siteMappingList);
        when(dciService.getDciDevices(any(AccountMapping.class), any(String.class), any(String.class))).thenReturn(deviceResponseDevice);
        this.mockMvc.perform(get("/some_account_uid/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("accountUid", "testAccountUid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].rmmSiteUid", is(dattoContinuityDevice.getRmmSiteUid())))
                .andExpect(jsonPath("$[0].serialNumber", is(dattoContinuityDevice.getSerialNumber())))
                .andExpect(jsonPath("$[0].name", is(dattoContinuityDevice.getName())));
    }

    @Test
    public void getAllDattoContinuityDeviceV2Test() throws Exception {
        when(accountMappingProcessor.checkAccountMapping(any(String.class))).thenReturn(accountMapping);
        when(siteMappingProcessor.getSiteMappings(any(String.class))).thenReturn(siteMappingList);
        when(dciService.getDciDevices(any(AccountMapping.class), any(String.class), any(String.class))).thenReturn(deviceResponseDevice);
        MvcResult result = this.mockMvc.perform(get("/v2/some_account_uid/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("accountUid", "testAccountUid"))
                .andExpect(status().isOk())
                .andReturn();

        DeviceResponse<DattoContinuityDevice> responseDevice = mapper.readValue(result.getResponse().getContentAsString(), new TypeReference<DeviceResponse<DattoContinuityDevice>>() {
        });
        assertEquals(responseDevice.getItems().size(), 1);
        assertEquals(responseDevice.getItems().get(0).getRmmSiteUid(), dattoContinuityDevice.getRmmSiteUid());
    }

    @Test
    public void getAgentsBySerialNumberTest() throws Exception {
        when(accountMappingProcessor.checkAccountMapping(any(String.class))).thenReturn(accountMapping);
        when(dciService.getDeviceAgents(any(AccountMapping.class), any(String.class), any(String.class), any(String.class))).thenReturn(deviceResponseAgent);
        mockMvc.perform(get("/some-account-uid/devices/some-serial-number}/agents")
                        .param("accountUid", "testAccountUid")
                        .param("serialNumber", "testSerialNumber"))
                .andExpect(status().isOk());
    }

    @Test
    public void getAgentsBySerialNumberV2Test() throws Exception {
        when(accountMappingProcessor.checkAccountMapping(any(String.class))).thenReturn(accountMapping);
        when(dciService.getDeviceAgents(any(AccountMapping.class), any(String.class), any(String.class), any(String.class))).thenReturn(deviceResponseAgent);
        MvcResult result = mockMvc.perform(get("/v2/some-account-uid/devices/some-serial-number}/agents")
                        .param("accountUid", "testAccountUid")
                        .param("serialNumber", "testSerialNumber"))
                .andExpect(status().isOk())
                .andReturn();
        DeviceResponse<AgentDto> responseDevice = mapper.readValue(result.getResponse().getContentAsString(), new TypeReference<DeviceResponse<AgentDto>>() {
        });
        assertEquals(responseDevice.getItems().size(), 1);
        assertEquals(responseDevice.getItems().get(0).getName(), agent.getName());
        assertEquals(responseDevice.getItems().get(0).getLocalIp(), agent.getLocalIp());
    }

    @Test
    public void getProtectedDeviceBySerialNumberTest() throws Exception {
        when(accountMappingProcessor.checkAccountMapping(any(String.class))).thenReturn(accountMapping);
        when(dciService.getDeviceAgents(any(AccountMapping.class), any(String.class), any(String.class), any(String.class))).thenReturn(deviceResponseAgent);
        MvcResult result = mockMvc.perform(get("/some-account-uid/devices/some-serial-number}/protected")
                        .param("accountUid", "testAccountUid")
                        .param("serialNumber", "testSerialNumber")
                        .queryParam("hostName", agent.getName())
                        .queryParam("internalIp", agent.getLocalIp()))
                .andExpect(status().isOk())
                .andReturn();
        AgentDto agentDto = mapper.readValue(result.getResponse().getContentAsString(), AgentDto.class);
        assertEquals(agentDto.getName(), agent.getName());
        assertEquals(agentDto.getLocalIp(), agent.getLocalIp());
    }

    @Test
    public void getProtectedDeviceBySerialNumberBadRequestTest() throws Exception {
        when(accountMappingProcessor.checkAccountMapping(any(String.class))).thenReturn(accountMapping);
        when(dciService.getDeviceAgents(any(AccountMapping.class), any(String.class), any(String.class), any(String.class))).thenReturn(deviceResponseAgent);
        mockMvc.perform(get("/some-account-uid/devices/some-serial-number}/protected")
                        .param("accountUid", "testAccountUid")
                        .param("serialNumber", "testSerialNumber"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void mapDciToProtectedDevice() throws Exception {
        mockMvc.perform(post("/testAccountUid/devices/mapped-devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("accountUid", "testAccountUid")
                        .content(convertToString(deviceMapping)))
                .andExpect(status().isOk());
    }

    @Test
    public void deleteMappedDevicesTest() throws Exception {
        mockMvc.perform(delete("/testAccountUid/devices/mapped-devices/testDciDeviceUid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("accountUid", "testAccountUid")
                        .param("dciDeviceUid", "testDciDeviceUid"))
                .andExpect(status().isOk());
    }

}
