package net.datto.dciservice.repository;

import net.datto.dciservice.dynamodb.DeviceMapping;
import net.datto.dciservice.dynamodb.DynamoDbDeviceMappingDao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class DynamoDeviceMappingTest extends AbstractRepositoryIntegrationTest {
    private static final String DEVICE_UID = "dci_device_uid";
    private static final String PROTECTED_DEVICE_UID = "protected_device_uid";
    private static final String SERIAL_NUMBER = "serial_number";

    private static final ScanRequest SCAN_REQUEST = ScanRequest.builder()
            .tableName("dci_device_mapping")
            .build();

    @Autowired
    private DynamoDbDeviceMappingDao mockDynamoDbDeviceMappingDao;

    @Test
    void shouldStoreDeviceMapping() throws Exception {
        // given
        var deviceUid = "1";
        var protectedDeviceUid = "5";
        var serialNumber = "1234569789";

        var deviceMapping = DeviceMapping.builder()
                .dciDeviceUid(deviceUid)
                .protectedDeviceUid(protectedDeviceUid)
                .serialNumber(serialNumber)
                .build();

        // when
        mockDynamoDbDeviceMappingDao.storeDeviceMapping(deviceMapping);

        // then
        var data = dynamoDbAsyncClient.scan(SCAN_REQUEST).get().items();

        assertThat(data).contains(Map.of(
                DEVICE_UID, AttributeValue.fromS(deviceUid),
                PROTECTED_DEVICE_UID, AttributeValue.fromS(protectedDeviceUid),
                SERIAL_NUMBER, AttributeValue.fromS(serialNumber)));
    }

    @Test
    void shouldDeleteByDeviceUid() throws Exception {
        // given
        var deviceUid = "100";

        // when
        mockDynamoDbDeviceMappingDao.deleteDeviceMapping(deviceUid);

        // then
        var data = dynamoDbAsyncClient.scan(SCAN_REQUEST).get().items();
        assertThat(data)
                .extracting(attribute -> attribute.get(DEVICE_UID))
                .doesNotContain(AttributeValue.fromS(deviceUid));
    }

    @Test
    void shouldDeleteByDeviceUidAndProtectedDeviceUid() throws Exception {
        // given
        var deviceUid = "269";
        var protectedDeviceUid = "441";

        // when
        mockDynamoDbDeviceMappingDao.deleteDeviceMapping(deviceUid, protectedDeviceUid);

        // then
        var data = dynamoDbAsyncClient.scan(SCAN_REQUEST).get().items();
        assertThat(data)
                .extracting(attribute -> attribute.get(DEVICE_UID))
                .doesNotContain(AttributeValue.fromS(deviceUid));
    }

    @Test
    void shouldNotDeleteByDeviceUidAndProtectedDeviceUidWhenInvalidData() throws Exception {
        // given
        var deviceUid = "439";
        var invalidProtectedDeviceUid = "invalid";

        // when
        mockDynamoDbDeviceMappingDao.deleteDeviceMapping(deviceUid, invalidProtectedDeviceUid);

        // then
        var data = dynamoDbAsyncClient.scan(SCAN_REQUEST).get().items();
        assertThat(data)
                .extracting(attribute -> attribute.get(DEVICE_UID))
                .contains(AttributeValue.fromS(deviceUid));
    }

}
