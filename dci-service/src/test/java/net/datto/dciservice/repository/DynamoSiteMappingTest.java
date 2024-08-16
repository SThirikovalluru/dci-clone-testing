package net.datto.dciservice.repository;

import net.datto.dciservice.dynamodb.DynamoDbSiteMappingDao;
import net.datto.dciservice.dynamodb.SiteMapping;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class DynamoSiteMappingTest extends AbstractRepositoryIntegrationTest {
    private static final String RMM_ACCOUNT_UID = "rmm_account_uid";
    private static final String SERIAL_NUMBER = "serial_number";
    private static final String SITE_UID = "rmm_site_uid";
    private static final String NAME = "name";
    private static final String MODEL = "model";
    private static final String INTERNAL_IP = "internal_ip";
    private static final String CLIENT_COMPANY_NAME = "client_company_name";

    private static final ScanRequest SCAN_REQUEST = ScanRequest.builder()
            .tableName("dci_site_mapping")
            .build();

    @Autowired
    private DynamoDbSiteMappingDao mockDynamoDbSiteMappingDao;

    @Test
    void shouldStoreSiteMapping() throws Exception {
        // given
        var rrmId = "100";
        var serialNumber = "555";
        var siteUid = "8";
        var name = "name";
        var model = "model";
        var internalIp = "localhost";
        var companyName = "kaseya";

        var accountMapping = SiteMapping.builder()
                .rmmAccountUid(rrmId)
                .serialNumber(serialNumber)
                .rmmSiteUid(siteUid)
                .name(name)
                .model(model)
                .internalIp(internalIp)
                .clientCompanyName(companyName)
                .build();

        // when
        mockDynamoDbSiteMappingDao.storeSiteMapping(accountMapping);

        // then
        var data = dynamoDbAsyncClient.scan(SCAN_REQUEST).get().items();

        assertThat(data).contains(Map.of(
                RMM_ACCOUNT_UID, AttributeValue.fromS(rrmId),
                SERIAL_NUMBER, AttributeValue.fromS(serialNumber),
                SITE_UID, AttributeValue.fromS(siteUid),
                NAME, AttributeValue.fromS(name),
                MODEL, AttributeValue.fromS(model),
                INTERNAL_IP, AttributeValue.fromS(internalIp),
                CLIENT_COMPANY_NAME, AttributeValue.fromS(companyName)));
    }

    @Test
    void shouldGetSiteMappingByRmmId() {
        // given
        var rrmId = "1";

        // when
        var rmmSites = mockDynamoDbSiteMappingDao.getRmmSitesMapped(rrmId);

        // then
        var expectedSiteMapping = SiteMapping.builder()
                .rmmAccountUid(rrmId)
                .serialNumber("233")
                .rmmSiteUid("456")
                .name("first")
                .model("ram")
                .internalIp("125.584.0.1")
                .clientCompanyName("kaseya")
                .build();

        assertThat(rmmSites).isEqualTo(List.of(expectedSiteMapping));
    }

    @Test
    void shouldGetSiteMappingByRmmIdAndSerialNumber() {
        // given
        var rrmId = "2";
        var serialNumber = "533";

        // when
        var siteMapping = mockDynamoDbSiteMappingDao.getSiteMapping(rrmId, serialNumber);

        // then
        var expectedSiteMapping = SiteMapping.builder()
                .rmmAccountUid(rrmId)
                .serialNumber(serialNumber)
                .rmmSiteUid("956")
                .name("second")
                .model("dodge")
                .internalIp("255.127.0.8")
                .clientCompanyName("datto")
                .build();

        assertThat(siteMapping).isEqualTo(expectedSiteMapping);
    }

    @Test
    void shouldDeleteByAccountUid() throws Exception{
        // given
        var rmmAccountId = "4";

        // when
        mockDynamoDbSiteMappingDao.deleteSiteMappingByAccountUid(rmmAccountId);

        // then
        var data = dynamoDbAsyncClient.scan(SCAN_REQUEST).get().items();
        assertThat(data)
                .extracting(attribute -> attribute.get(RMM_ACCOUNT_UID))
                .doesNotContain(AttributeValue.fromS(rmmAccountId));
    }

    @Test
    void shouldSiteMapping() throws Exception{
        // given
        var rmmAccountId = "5";

        var siteMapping = SiteMapping.builder()
                .rmmAccountUid(rmmAccountId)
                .serialNumber("45369874")
                .rmmSiteUid("741")
                .name("name")
                .model("schelby")
                .internalIp("127.1.0.8")
                .clientCompanyName("kaseya")
                .build();

        // when
        mockDynamoDbSiteMappingDao.deleteSiteMapping(siteMapping);

        // then
        var data = dynamoDbAsyncClient.scan(SCAN_REQUEST).get().items();
        assertThat(data)
                .extracting(attribute -> attribute.get(RMM_ACCOUNT_UID))
                .doesNotContain(AttributeValue.fromS(rmmAccountId));
    }
}
