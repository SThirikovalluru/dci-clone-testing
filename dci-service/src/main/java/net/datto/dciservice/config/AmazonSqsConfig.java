package net.datto.dciservice.config;


import io.awspring.cloud.sqs.config.SqsMessageListenerContainerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import static io.awspring.cloud.sqs.listener.acknowledgement.handler.AcknowledgementMode.ON_SUCCESS;

@RequiredArgsConstructor
@Configuration
public class AmazonSqsConfig {
    private final SqsAsyncClient sqsAsyncClient;

    @Bean
    public SqsMessageListenerContainerFactory<Object> defaultSqsListenerContainerFactory() {
        return SqsMessageListenerContainerFactory
                .builder()
                .configure(options -> options.acknowledgementMode(ON_SUCCESS))
                .sqsAsyncClient(sqsAsyncClient)
                .build();
    }

}
