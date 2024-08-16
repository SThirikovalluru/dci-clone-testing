package net.datto.dciservice.dynamodb;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class AccountMapping {
    @Getter(onMethod = @__({@DynamoDbPartitionKey, @DynamoDbAttribute("rmm_account_uid")}))
    private String rmmAccountUid;
    @Getter(onMethod = @__({@DynamoDbAttribute("portal_public_key")}))
    private String portalPublicKey = "";
    @Getter(onMethod = @__({@DynamoDbAttribute("portal_secret_key")}))
    private String portalSecretKey = "";
}