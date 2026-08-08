package io.github.khushaalsharma.ratelimiter.autoconfigure.analytics;

import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;

import java.time.Instant;

public class KafkaRateLimitEventPublisher implements RateLimitEventPublisher{
    private final KafkaTemplate<String, RateLimitEventDto> kafkaTemplate;
    private final String topic;
    private final String groupId;

    public KafkaRateLimitEventPublisher(KafkaTemplate<String, RateLimitEventDto> kafkaTemplate, String topic, String groupId) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
        this.groupId = groupId;
    }

    @Override
    public void publish(RateLimitEventDto event) {
        System.out.println(">>>> SENDING EVENT FOR KAFKA RATE LIMIT");
        String correlationId = MDC.get("correlationId");

        var message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, topic)
                .setHeader(KafkaHeaders.GROUP_ID, groupId)
                .setHeader(KafkaHeaders.KEY, event.getClientKey())
                .setHeader("correlationId", correlationId != null ? correlationId : "unknown-" + Instant.now().toString())
                .build();

        kafkaTemplate.send(message).whenComplete((result, ex) -> {
            if (ex != null) {
                System.out.println(">>>> KAFKA SEND FAILED: " + ex.getMessage());
                ex.printStackTrace();
            } else {
                System.out.println(">>>> KAFKA SEND SUCCEEDED: offset=" + result.getRecordMetadata().offset());
            }
        });
    }
}
