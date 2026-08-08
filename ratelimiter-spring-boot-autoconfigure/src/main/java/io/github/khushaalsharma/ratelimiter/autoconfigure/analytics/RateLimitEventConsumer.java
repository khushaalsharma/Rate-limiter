package io.github.khushaalsharma.ratelimiter.autoconfigure.analytics;

import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.time.Instant;

/*
* Consume is formed
* listener function is not working
* check it
* not repository bean problem,
* possible deserialization
* */

@Component
public class RateLimitEventConsumer {

    private final RateLimitEventRepository rateLimitEventRepository;

    public RateLimitEventConsumer(RateLimitEventRepository rateLimitEventRepository) {
        System.out.println(">>>KAFKA CONSUMER CONSTRUCTOR CALLED");
        System.out.println("Topic: " + "#{@rateLimiterProperties.analytics.topic}");
        this.rateLimitEventRepository = rateLimitEventRepository;
    }

    //@KafkaListener(topics = "#{@rateLimiterProperties.analytics.topic}")
    @KafkaListener(
            topics = "rate-limit-events",
            groupId = "ratelimiter-analytics"
    )
    public void onEvent(RateLimitEventDto dto, @Header(value = "correlationId", required = false) String correlationId){

        System.out.println("Executing consumer for correlationId: " + correlationId);
        System.out.println(">>>> Consumer for: " + dto.getEventId() + ", " + dto.getEndpoint() + ", by Client: " + dto.getClientKey());

        MDC.put("correlationId", correlationId != null ? correlationId : "unknown-" + Instant.now().toString());
        try {
            if (rateLimitEventRepository.findByEventId(dto.getEventId()).isPresent()) {
                return; // already persisted, skip duplicate delivery
            }

            RateLimitEvent event = new RateLimitEvent(
                    dto.getEventId(),           // FIX: this was missing in the original — eventId now correctly set
                    dto.getClientKey(),
                    dto.getEndpoint(),
                    dto.isAllowed(),
                    dto.getAlgorithm(),
                    dto.getRemainingRequests(),
                    dto.getTimestamp()
            );

            rateLimitEventRepository.saveAndFlush(event);
        } finally {
            MDC.remove("correlationId");
        }
    }
}
