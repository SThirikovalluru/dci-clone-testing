package net.datto.dciservice.configuration;

import net.datto.dciservice.dynamodb.DynamoDbAccountMapping;
import net.datto.dciservice.dynamodb.DynamoDbAccountMappingDao;
import net.datto.dciservice.dynamodb.DynamoDbDeviceMapping;
import net.datto.dciservice.dynamodb.DynamoDbDeviceMappingDao;
import net.datto.dciservice.dynamodb.DynamoDbSiteMapping;
import net.datto.dciservice.dynamodb.DynamoDbSiteMappingDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;

@TestConfiguration
public class RepositoryTestConfiguration {

    @Autowired
    private DynamoDbEnhancedClient mockdynamoDbEnhancedClient;

    @Bean
    public DynamoDbAccountMapping mockDynamoDbAccountMapping() {
        return new DynamoDbAccountMapping(mockdynamoDbEnhancedClient);
    }

    @Bean
    public DynamoDbDeviceMapping mockDynamoDbDeviceMapping() {
        return new DynamoDbDeviceMapping(mockdynamoDbEnhancedClient);
    }

    @Bean
    public DynamoDbSiteMapping mockDynamoDbSiteMapping() {
        return new DynamoDbSiteMapping(mockdynamoDbEnhancedClient);
    }

    @Bean
    public DynamoDbAccountMappingDao mockDynamoDbAccountMappingDao(DynamoDbAccountMapping mockDynamoDbAccountMapping) {
        return new DynamoDbAccountMappingDao(mockDynamoDbAccountMapping);
    }

    @Bean
    public DynamoDbDeviceMappingDao mockDynamoDbDeviceMappingDao(DynamoDbDeviceMapping mockDynamoDbDeviceMapping) {
        return new DynamoDbDeviceMappingDao(mockDynamoDbDeviceMapping);
    }

    @Bean
    public DynamoDbSiteMappingDao mockDynamoDbSiteMappingDao(DynamoDbSiteMapping mockDynamoDbSiteMapping) {
        return new DynamoDbSiteMappingDao(mockDynamoDbSiteMapping);
    }
}
