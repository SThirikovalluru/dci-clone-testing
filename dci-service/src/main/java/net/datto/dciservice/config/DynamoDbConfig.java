package net.datto.dciservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;
import java.util.function.Supplier;

/**
 * Configuration class for the DynamoDB.
 */
@Configuration
public class DynamoDbConfig {

    @Value("${dynamodb.endPoint:#{null}}")
    private String endPoint;

    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient dynamoDbClient) {
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
    }

    @Bean
    @ConditionalOnProperty(value = "dci.localstack.enabled", havingValue = "false")
    public DynamoDbClient dynamoDbClient() {
        // this is an optional setting, usually set when using the local dynamo db instance
        return DynamoDbClient.builder()
                .endpointOverride(endPoint != null ? URI.create(endPoint) : null)
                .build();
    }

}
