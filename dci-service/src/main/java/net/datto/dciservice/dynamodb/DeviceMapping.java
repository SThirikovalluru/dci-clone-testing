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
public class DeviceMapping {
    @Getter(onMethod = @__({@DynamoDbPartitionKey, @DynamoDbAttribute("dci_device_uid")}))
    private String dciDeviceUid;
    @Getter(onMethod = @__({@DynamoDbSortKey, @DynamoDbAttribute("protected_device_uid")}))
    private String protectedDeviceUid;
    @Getter(onMethod = @__({@DynamoDbAttribute("serial_number")}))
    private String serialNumber;
}
