package net.datto.dciservice.controllers;

import net.datto.dci.api.dto.DattoContinuityDevice;
import net.datto.dciservice.configuration.SpringTestConfig;
import net.datto.dciservice.dynamodb.AccountMapping;
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

import java.util.LinkedList;
import java.util.List;

import static net.datto.dciservice.utils.JsonParser.convertToString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;


@SpringBootTest
@Import(SpringTestConfig.class)
public class SiteControllerTest {

    @Autowired
    SiteController siteController;
    @MockBean
    SiteMappingProcessor siteMappingProcessor;
    @MockBean
    AccountMappingProcessor accountMappingProcessor;
    @MockBean
    DciService dciService;

    DattoContinuityDevice dattoContinuityDevice;
    List<SiteMapping> siteMappingList;
    SiteMapping siteMapping;
    AccountMapping accountMapping;
    NotFoundException exception;
    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        this.mockMvc = standaloneSetup(this.siteController).setControllerAdvice(new ExceptionController()).build();
        dattoContinuityDevice = new DattoContinuityDevice();
        dattoContinuityDevice.setSerialNumber("testSerialNumber");
        dattoContinuityDevice.setName("testName");
        dattoContinuityDevice.setModel("TestModel");
        dattoContinuityDevice.setInternalIpAddress("testInternalIpAddress");
        dattoContinuityDevice.setClientCompanyName("testClientCompanyName");
        dattoContinuityDevice.setRmmSiteUid("testRmmSiteUid");
        siteMappingList = new LinkedList<>();
        siteMapping = new SiteMapping();
        siteMapping.setRmmAccountUid("setRmmAccountUid");
        siteMapping.setRmmAccountUid("testRmmAccountUid");
        siteMapping.setSerialNumber("testSerialNumber");
        siteMappingList.add(siteMapping);
        accountMapping = new AccountMapping();
        exception = new NotFoundException("Test NotFoundException");
    }

    @Test
    public void saveSiteMappingsTest() throws Exception {
        when(accountMappingProcessor.checkAccountMapping(any(String.class))).thenReturn(accountMapping);
        when(dciService.getDciDeviceBySerialNumber(any(AccountMapping.class), any(String.class))).thenReturn(dattoContinuityDevice);
        this.mockMvc.perform(post("/some-account-uid/site-mappings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("accountUid", "testAccountUid")
                        .content(convertToString(siteMappingList)))
                .andExpect(status().isOk());
    }


    @Test
    public void saveSiteMappingsTestBadRequestException() throws Exception {
        when(accountMappingProcessor.checkAccountMapping(any(String.class))).thenReturn(accountMapping);
        when(dciService.getDciDeviceBySerialNumber(any(AccountMapping.class), any(String.class))).thenReturn(dattoContinuityDevice);
        this.mockMvc.perform(post("/some-account-uid/site-mappings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertToString(null)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void deleteSiteMappingsTest() throws Exception {
        this.mockMvc.perform(delete("/some-account-uid/site-mappings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertToString(siteMappingList)))
                .andExpect(status().isOk());
    }


    @Test
    public void deleteSiteMappingsBySerialNumberTest() throws Exception {
        when(accountMappingProcessor.checkAccountMapping(any(String.class))).thenThrow(exception);
        this.mockMvc.perform(delete("/some-account-uid/site-mappings/some-serial-number")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }


    @Test
    public void togglesIntegrationTest() throws Exception {
        when(siteMappingProcessor.getSiteMappings(any(String.class))).thenReturn(siteMappingList);
        this.mockMvc.perform(post("/some-account-uid/integration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("accountUid", "testAccountUid")
                        .queryParam("enabled", "true"))
                .andExpect(status().isOk());
    }


}
