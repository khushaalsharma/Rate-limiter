package io.github.khushaalsharma.ratelimiter.core;

public record RateLimitDecision(
        boolean allowed,
        long remainingRequests,
        long resetAfterSeconds
) {
}