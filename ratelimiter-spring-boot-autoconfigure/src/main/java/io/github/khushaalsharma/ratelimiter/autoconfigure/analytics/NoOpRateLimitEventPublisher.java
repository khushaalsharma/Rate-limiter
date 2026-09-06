package io.github.khushaalsharma.ratelimiter.autoconfigure.analytics;

public class NoOpRateLimitEventPublisher implements RateLimitEventPublisher{
    @Override
    public void publish(RateLimitEventDto event) {
        //intentionally does nothing as s fail safe for those situations where kafka or analytics is not enabled
        System.out.println(">>> Event published for NoOp");
    }
}
