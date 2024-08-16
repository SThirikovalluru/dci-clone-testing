package net.datto.dciservice.dynamodb;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class SiteMapping {
    @Getter(onMethod = @__({@DynamoDbPartitionKey, @DynamoDbAttribute("rmm_account_uid")}))
    private String rmmAccountUid;
    @Getter(onMethod = @__({@DynamoDbAttribute("rmm_site_uid")}))
    private String rmmSiteUid;
    @Getter(onMethod = @__({@DynamoDbSortKey, @DynamoDbAttribute("serial_number")}))
    private String serialNumber;
    @Getter(onMethod = @__({@DynamoDbAttribute("name")}))
    private String name;
    @Getter(onMethod = @__({@DynamoDbAttribute("model")}))
    private String model;
    @Getter(onMethod = @__({@DynamoDbAttribute("internal_ip")}))
    private String internalIp;
    @Getter(onMethod = @__({@DynamoDbAttribute("client_company_name")}))
    private String clientCompanyName;
}