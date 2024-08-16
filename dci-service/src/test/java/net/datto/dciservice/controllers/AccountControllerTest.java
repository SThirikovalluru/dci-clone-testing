package net.datto.dciservice.controllers;

import net.datto.dciservice.configuration.SpringTestConfig;
import net.datto.dciservice.dynamodb.AccountMapping;
import net.datto.dciservice.services.AccountMappingProcessor;
import net.datto.dciservice.utils.ExceptionController;
import net.datto.dciservice.utils.InternalServerErrorException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;


@SpringBootTest
@Import(SpringTestConfig.class)
public class AccountControllerTest {

    MockMvc mockMvc;
    @MockBean
    AccountMappingProcessor accountMappingProcessor;
    @Autowired
    AccountController accountController;
    AccountMapping accountMapping;

    @BeforeEach
    public void setup() {
        this.mockMvc = standaloneSetup(this.accountController).setControllerAdvice(new ExceptionController()).build();
        accountMapping = new AccountMapping();
    }

    @Test
    public void authenticateTest() throws Exception {
        when(accountMappingProcessor.getAccountMapping(any(String.class))).thenReturn(accountMapping);
        mockMvc.perform(get("/authentication")
                        .param("accountUid", "testAccountUid")
                        .param("publicKey", "testPublicKey")
                        .param("privateKey", "testPrivateKey")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void authenticateTestBadRequest() throws Exception {
        when(accountMappingProcessor.getAccountMapping(any(String.class))).thenReturn(null);
        mockMvc.perform(get("/authentication")
                        .param("accountUid", "testAccountUid")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void authenticateTestInternalServerError() throws Exception {
        InternalServerErrorException httpClientErrorException = mock(InternalServerErrorException.class);
        when(accountMappingProcessor.getAccountMapping(any(String.class))).thenReturn(accountMapping);
        when(accountMappingProcessor.doAuthentication(accountMapping)).thenThrow(httpClientErrorException);
        mockMvc.perform(get("/authentication")
                        .param("accountUid", "testAccountUid")
                        .param("publicKey", "testPublicKey")
                        .param("privateKey", "testPrivateKey")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message", startsWith("Authentication failed for RMM accountUid: ")));
    }

    @Test
    public void getAccountMappingTest() throws Exception {
        when(accountMappingProcessor.checkAccountMapping(any(String.class))).thenReturn(accountMapping);
        mockMvc.perform(get("/some-account-uid/account-mappings")
                        .param("accountUid", "testAccountUid")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void checkAccountMappingTest() throws Exception {
        when(accountMappingProcessor.checkAccountMapping(any(String.class))).thenReturn(accountMapping);
        mockMvc.perform(get("/some-account-uid/validate-account-mappings")
                        .param("accountUid", "testAccountUid")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void deleteAccountMappingTest() throws Exception {
        when(accountMappingProcessor.checkAccountMapping(any(String.class))).thenReturn(accountMapping);
        mockMvc.perform(delete("/some-account-uid/account-mappings")
                        .param("accountUid", "testAccountUid")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
