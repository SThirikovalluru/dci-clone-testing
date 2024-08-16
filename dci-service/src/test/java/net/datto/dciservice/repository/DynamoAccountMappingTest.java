package net.datto.dciservice.repository;

import net.datto.dciservice.dynamodb.AccountMapping;
import net.datto.dciservice.dynamodb.DynamoDbAccountMappingDao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class DynamoAccountMappingTest extends AbstractRepositoryIntegrationTest {
    private static final String RMM_ACCOUNT_UID = "rmm_account_uid";
    private static final String PORTAL_PUBLIC_KEY = "portal_public_key";
    private static final String PORTAL_SECRET_KEY = "portal_secret_key";

    private static final ScanRequest SCAN_REQUEST = ScanRequest.builder()
            .tableName("datto_integrations_info")
            .build();

    @Autowired
    private DynamoDbAccountMappingDao mockDynamoDbAccountMappingDao;

    @Test
    void shouldStoreAccountMapping() throws Exception {
        // given
        var rrmId = "100";
        var publicKey = "987";
        var secretKey = "secret";

        var accountMapping = AccountMapping.builder()
                .rmmAccountUid(rrmId)
                .portalPublicKey(publicKey)
                .portalSecretKey(secretKey)
                .build();

        // when
        mockDynamoDbAccountMappingDao.storeAccountMapping(accountMapping);

        // then
        var data = dynamoDbAsyncClient.scan(SCAN_REQUEST).get().items();

        assertThat(data).contains(Map.of(
                RMM_ACCOUNT_UID, AttributeValue.fromS(rrmId),
                PORTAL_PUBLIC_KEY, AttributeValue.fromS(publicKey),
                PORTAL_SECRET_KEY, AttributeValue.fromS(secretKey)));
    }

    @Test
    public void shouldGetRmmAccountId() {
        // when
        var rmmAccountId = "1";
        var publicKey = "10";
        var secretKey = "10-secret";
        var accountMapping = mockDynamoDbAccountMappingDao.getAccountMapping(rmmAccountId);

        // then
        var expectedAccountMapping = AccountMapping.builder()
                .rmmAccountUid(rmmAccountId)
                .portalPublicKey(publicKey)
                .portalSecretKey(secretKey)
                .build();

        assertThat(accountMapping).isEqualTo(expectedAccountMapping);
    }

    @Test
    void shouldDeleteByDeviceId() throws Exception {
        // given
        var rmmAccountId = "5";

        // when
        mockDynamoDbAccountMappingDao.deleteAccountMapping(rmmAccountId);

        // then
        var data = dynamoDbAsyncClient.scan(SCAN_REQUEST).get().items();
        assertThat(data)
                .extracting(attribute -> attribute.get(RMM_ACCOUNT_UID))
                .doesNotContain(AttributeValue.fromS(rmmAccountId));
    }

}
