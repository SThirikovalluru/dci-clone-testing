package net.datto.dciservice.dynamodb;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class DeviceMappingProcessor {
    private final DynamoDbDeviceMappingDao dynamoDbDeviceMappingDao;

    public void saveDeviceMapping(DeviceMapping deviceMapping) {
        Optional.ofNullable(deviceMapping)
                .ifPresent(dynamoDbDeviceMappingDao::storeDeviceMapping);
    }

    public void deleteDeviceMapping(String dciDeviceUid, String protectedDeviceUid) {
        dynamoDbDeviceMappingDao.deleteDeviceMapping(dciDeviceUid, protectedDeviceUid);
    }

    public void deleteDeviceMapping(String dciDeviceUid) {
        dynamoDbDeviceMappingDao.deleteDeviceMapping(dciDeviceUid);
    }
}
