package net.datto.dciservice.dynamodb;

import org.springframework.stereotype.Component;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static software.amazon.awssdk.enhanced.dynamodb.internal.AttributeValues.stringValue;

@Component
public class DynamoDbDeviceMapping {
    private static final String TABLE_NAME = "dci_device_mapping";

    private final DynamoDbTable<DeviceMapping> deviceMappingTable;
    private final DynamoDbEnhancedClient dynamoDbEnhancedClient;

    public DynamoDbDeviceMapping(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        this.dynamoDbEnhancedClient = dynamoDbEnhancedClient;
        this.deviceMappingTable = dynamoDbEnhancedClient.table(TABLE_NAME, TableSchema.fromBean(DeviceMapping.class));
    }

    /**
     * Gets DynamoDb entry.
     */
    public List<DeviceMapping> getByDciDeviceUid(String dciDeviceUid) {
        var key = Key.builder()
                .partitionValue(dciDeviceUid)
                .build();

        return deviceMappingTable.query(QueryConditional.keyEqualTo(key))
                .items()
                .stream()
                .toList();
    }

    /**
     * Save the supplied object as a DynamoDb entry.
     */
    public void save(DeviceMapping deviceMapping) {
        deviceMappingTable.putItem(deviceMapping);
    }

    /**
     * Deletes data by the RMM deviceUid for the DCI device
     */
    public void deleteByDciDeviceUid(String dciDeviceUid) {
        deleteDeviceMapping(() -> getByDciDeviceUid(dciDeviceUid));
    }

    public void deleteDeviceMapping(String dciDeviceUid, String protectedDeviceUid) {
        var deviceMappintKey = Key.builder()
                .partitionValue(dciDeviceUid)
                .sortValue(protectedDeviceUid)
                .build();

        deviceMappingTable.deleteItem(deviceMappintKey);
    }

    private void deleteDeviceMapping(Supplier<List<DeviceMapping>> siteMappings) {
        Optional.ofNullable(siteMappings.get())
                .filter(Predicate.not(List::isEmpty))
                .map(this::generateBatchWriteItemEnhancedRequest)
                .ifPresent(dynamoDbEnhancedClient::batchWriteItem);
    }

    private BatchWriteItemEnhancedRequest generateBatchWriteItemEnhancedRequest(List<DeviceMapping> deviceMappings) {
        var batch = deviceMappings.stream()
                .map(deviceMapping -> WriteBatch.builder(DeviceMapping.class)
                        .mappedTableResource(deviceMappingTable)
                        .addDeleteItem(deviceMapping)
                        .build())
                .toList();

        return BatchWriteItemEnhancedRequest.builder()
                .writeBatches(batch)
                .build();
    }
}
