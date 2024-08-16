package net.datto.dciservice.configuration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import static net.datto.dciservice.repository.AbstractRepositoryIntegrationTest.LOCAL_STACK_CONTAINER;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.DYNAMODB;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SQS;

@TestConfiguration
public class AwsLocalstackConfiguration {

    @Bean
    public DynamoDbClient mockdynamoDbClient() {
        return DynamoDbClient.builder()
                .endpointOverride(LOCAL_STACK_CONTAINER.getEndpointOverride(DYNAMODB))
                .region(Region.of(LOCAL_STACK_CONTAINER.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(LOCAL_STACK_CONTAINER.getAccessKey(), LOCAL_STACK_CONTAINER.getSecretKey())))
                .build();
    }

    @Bean
    public SqsAsyncClient mockSqsAsyncClient() {
        return SqsAsyncClient.builder()
                .region(Region.of(LOCAL_STACK_CONTAINER.getRegion()))
                .endpointOverride(LOCAL_STACK_CONTAINER.getEndpointOverride(SQS))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(LOCAL_STACK_CONTAINER.getAccessKey(), LOCAL_STACK_CONTAINER.getSecretKey())))
                .build();
    }
}
