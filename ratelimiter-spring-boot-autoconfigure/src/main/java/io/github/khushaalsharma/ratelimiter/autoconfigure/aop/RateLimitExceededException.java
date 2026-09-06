package io.github.khushaalsharma.ratelimiter.autoconfigure.aop;

import io.github.khushaalsharma.ratelimiter.core.RateLimitDecision;

public class RateLimitExceededException extends RuntimeException{
    private final RateLimitDecision decision;

    public RateLimitExceededException(RateLimitDecision decision) {
        super("Rate limit exceeded. Remaining: " + decision.remainingRequests());
        this.decision = decision;
    }

    public RateLimitDecision decision() {
        return decision;
    }
}
