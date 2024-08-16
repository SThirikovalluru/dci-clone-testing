package net.datto.dciservice.dynamodb;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class DynamoDbAccountMappingDao {
    private final DynamoDbAccountMapping dynamoDbAccountMapping;

    public void storeAccountMapping(AccountMapping accountMapping) {
        dynamoDbAccountMapping.save(accountMapping);
    }

    public AccountMapping getAccountMapping(String rmmAccountUid) {
        return dynamoDbAccountMapping.get(rmmAccountUid);
    }

    public void deleteAccountMapping(String accountUid) {
        dynamoDbAccountMapping.delete(accountUid);
    }


}

