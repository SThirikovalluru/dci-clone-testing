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
public class DynamoDbSiteMapping {
    private static final String TABLE_NAME = "dci_site_mapping";

    private final DynamoDbTable<SiteMapping> siteMappingTable;
    private final DynamoDbEnhancedClient dynamoDbEnhancedClient;

    public DynamoDbSiteMapping(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        this.dynamoDbEnhancedClient = dynamoDbEnhancedClient;
        this.siteMappingTable = dynamoDbEnhancedClient.table(TABLE_NAME, TableSchema.fromBean(SiteMapping.class));
    }

    /**
     * Save the supplied object as a DynamoDb entry.
     */
    public void save(SiteMapping siteMapping) {
        siteMappingTable.putItem(siteMapping);
    }

    /**
     * Gets DynamoDb entries.
     */
    public SiteMapping getByAccountAndSerialNumber(String rmmAccountUid, String serialNumber) {
        var deviceMappintKey = Key.builder()
                .partitionValue(rmmAccountUid)
                .sortValue(serialNumber)
                .build();

        return siteMappingTable.getItem(deviceMappintKey);
    }

    /**
     * Gets DynamoDb entry.
     */
    public List<SiteMapping> getByAccount(String rmmAccountUid) {
        var key = Key.builder()
                .partitionValue(rmmAccountUid)
                .build();

        return siteMappingTable.query(QueryConditional.keyEqualTo(key))
                .items()
                .stream()
                .toList();
    }


    /**
     * Deletes data by the RMM accountUID
     */
    public void deleteByAccountUid(String rmmAccountUid) {
        deleteSiteMapping(() -> getByAccount(rmmAccountUid));
    }

    public void deleteSiteMapping(SiteMapping siteMapping) {
        siteMappingTable.deleteItem(siteMapping);
    }

    private void deleteSiteMapping(Supplier<List<SiteMapping>> siteMappings) {
        Optional.ofNullable(siteMappings.get())
                .filter(Predicate.not(List::isEmpty))
                .map(this::generateBatchWriteItemEnhancedRequest)
                .ifPresent(dynamoDbEnhancedClient::batchWriteItem);
    }

    private BatchWriteItemEnhancedRequest generateBatchWriteItemEnhancedRequest(List<SiteMapping> siteMappings) {
        var batch = siteMappings.stream()
                .map(siteMapping -> WriteBatch.builder(SiteMapping.class)
                        .mappedTableResource(siteMappingTable)
                        .addDeleteItem(siteMapping)
                        .build())
                .toList();

        return BatchWriteItemEnhancedRequest.builder()
                .writeBatches(batch)
                .build();
    }
}
