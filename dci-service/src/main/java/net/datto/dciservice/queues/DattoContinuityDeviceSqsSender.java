package net.datto.dciservice.queues;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datto.dci.api.dto.DattoContinuityDeviceDto;
import net.datto.dciservice.services.MetricsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.UUID;

/**
 * RmmNotificationSqsSender - sending notifications into *DattoContinuity* queue for CSM.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class DattoContinuityDeviceSqsSender {

    private final SqsAsyncClient sqsAsyncClient;
    private final MetricsService metricsService;
    private final ObjectMapper mapper;

    @Value("${sqs.queues.dattoContinuityDeviceQueue}")
    private String dattoContinuityDeviceQueue;

    public void send(DattoContinuityDeviceDto notificationDto) {
        try {
            if (notificationDto != null) {
                log.debug("Received NotificationDto: {} for queue: {}", notificationDto, dattoContinuityDeviceQueue);
            }
            var messageRequest = SendMessageRequest.builder()
                    .queueUrl(dattoContinuityDeviceQueue)
                    .messageGroupId(notificationDto.getRmmAccountUid())
                    .messageDeduplicationId(UUID.randomUUID().toString())
                    .messageBody(mapper.writeValueAsString(notificationDto))
                    .build();

            sqsAsyncClient.sendMessage(messageRequest);
        } catch (JsonProcessingException e) {
            log.error("Failed to process Datto Contiuity Device notification message: {}", notificationDto, e);
            metricsService.markMeter(this.getClass(), "send.JsonProcessingException");
        } catch (SdkException e) {
            log.error("Failed to send Datto Contiuity Device notification message: {} to queue: {}", notificationDto, dattoContinuityDeviceQueue, e);
            metricsService.markMeter(this.getClass(), "send.AmazonClientException");
        }
    }
}
