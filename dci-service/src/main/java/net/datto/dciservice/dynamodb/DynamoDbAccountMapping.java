package net.datto.dciservice.dynamodb;

import org.springframework.stereotype.Component;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Component
public class DynamoDbAccountMapping {
    private static final String TABLE_NAME = "datto_integrations_info";

    private final DynamoDbTable<AccountMapping> accountMappingTable;

    public DynamoDbAccountMapping(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        this.accountMappingTable = dynamoDbEnhancedClient.table(TABLE_NAME, TableSchema.fromBean(AccountMapping.class));
    }

    /**
     * Save the supplied object as a DynamoDb entry.
     */
    public void save(AccountMapping accountMapping) {
        accountMappingTable.putItem(accountMapping);
    }

    /**
     * Gets DynamoDb entry.
     */
    public AccountMapping get(String rmmAccountUid) {
        var accountMappingKey = Key.builder()
                .partitionValue(rmmAccountUid)
                .build();

        return accountMappingTable.getItem(accountMappingKey);
    }

    /**
     * Deletes data by the RMM accountUID
     */
    public void delete(String rmmAccountUid) {
        var accountMappingKey = Key.builder()
                .partitionValue(rmmAccountUid)
                .build();

        accountMappingTable.deleteItem(accountMappingKey);
    }

}
