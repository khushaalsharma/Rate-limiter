package io.github.khushaalsharma.ratelimiter.autoconfigure.analytics;

import org.springframework.kafka.annotation.KafkaListener;

public class RateLimitEventConsumer {

    private final RateLimitEventRepository rateLimitEventRepository;

    public RateLimitEventConsumer(RateLimitEventRepository rateLimitEventRepository) {
        this.rateLimitEventRepository = rateLimitEventRepository;
    }

    @KafkaListener(topics = "#{@rateLimiterProperties.analytics.topic}")
    public void onEvent(RateLimitEventDto dto){
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

        rateLimitEventRepository.save(event);
    }
}
