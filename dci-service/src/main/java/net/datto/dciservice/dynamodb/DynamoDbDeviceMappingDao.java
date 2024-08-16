package net.datto.dciservice.dynamodb;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class DynamoDbDeviceMappingDao {
    private final DynamoDbDeviceMapping dynamoDbDeviceMapping;

    public void storeDeviceMapping(DeviceMapping deviceMapping) {
        dynamoDbDeviceMapping.save(deviceMapping);
    }

    public void deleteDeviceMapping(String dciDeviceUid, String protectedDeviceUid) {
        dynamoDbDeviceMapping.deleteDeviceMapping(dciDeviceUid, protectedDeviceUid);
    }

    public void deleteDeviceMapping(String dciDeviceUid) {
        dynamoDbDeviceMapping.deleteByDciDeviceUid(dciDeviceUid);
    }
}
