package io.github.khushaalsharma.ratelimiter.autoconfigure.analytics;

public interface RateLimitEventPublisher {
    void publish(RateLimitEventDto event);
}
