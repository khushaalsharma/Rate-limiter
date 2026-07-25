package io.github.khushaalsharma.ratelimiter.autoconfigure.analytics;

import org.springframework.kafka.core.KafkaTemplate;

public class KafkaRateLimitEventPublisher implements RateLimitEventPublisher{
    private final KafkaTemplate<String, RateLimitEventDto> kafkaTemplate;
    private final String topic;

    public KafkaRateLimitEventPublisher(KafkaTemplate<String, RateLimitEventDto> kafkaTemplate, String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    public void publish(RateLimitEventDto event) {
        kafkaTemplate.send(topic, event.getClientKey(), event);
    }
}
