package net.datto.dciservice.services;


import io.micrometer.core.instrument.Timer;
import net.datto.dciservice.dynamodb.AccountMapping;
import net.datto.dciservice.dynamodb.DynamoDbAccountMappingDao;
import net.datto.dciservice.utils.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AccountMappingProcessorTest {
    private static final AccountMapping ACCOUNT_MAPPING = AccountMapping.builder()
            .rmmAccountUid("rmmAccountId")
            .portalPublicKey("publicKey")
            .portalSecretKey("secretKey")
            .build();

    @Mock
    private DynamoDbAccountMappingDao dynamoDbAccountMappingDaoMock;
    @Mock
    private MetricsService metricsServiceMock;
    @Mock
    private Timer timer;
    @InjectMocks
    private AccountMappingProcessor accountMappingProcessor;

    @Test
    void shouldNotSaveMapping() {
        // when
        accountMappingProcessor.saveAccountMapping(null);

        // then
        verifyNoInteractions(dynamoDbAccountMappingDaoMock);
    }

    @Test
    void shouldSaveMapping() {
        // when
        accountMappingProcessor.saveAccountMapping(ACCOUNT_MAPPING);

        // then
        verify(dynamoDbAccountMappingDaoMock).storeAccountMapping(ACCOUNT_MAPPING);
    }

    @Test
    void shouldThrowExceptionWhenRmmNotFoundDuringCheckAccount() {
        // given
        var rmmAccountId = "rmmAccountId";

        // then
        assertThatThrownBy(() -> accountMappingProcessor.checkAccountMapping(rmmAccountId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Account information not found for accountUid: " + rmmAccountId);

        verify(metricsServiceMock).markMeter(AccountMappingProcessor.class, "checkAccountMapping.rmmAccUID.notFound." + rmmAccountId);
    }

    @Test
    void shouldReturnAccountMapping() throws NotFoundException {
        // given
        var rmmAccountId = "rmmAccountId";

        when(dynamoDbAccountMappingDaoMock.getAccountMapping(rmmAccountId))
                .thenReturn(ACCOUNT_MAPPING);

        // when
        var accountMapping = accountMappingProcessor.checkAccountMapping(rmmAccountId);

        // then
        assertThat(accountMapping).isEqualTo(ACCOUNT_MAPPING);
    }

    @Test
    void shouldDoAuthentication() throws Exception {
        // given
        when(metricsServiceMock.startTimer(AccountMappingProcessor.class, "doAuthentication.getDciDevices")).thenReturn(timer);

        // when
        var isAuthenticated = accountMappingProcessor.doAuthentication(ACCOUNT_MAPPING);

        // then
        assertThat(isAuthenticated).isTrue();
    }
}
