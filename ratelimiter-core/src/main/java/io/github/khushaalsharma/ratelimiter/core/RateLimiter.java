package io.github.khushaalsharma.ratelimiter.core;

import java.util.Map;

public class RateLimiter{
    private final Map<RateLimitAlgorithmType, RateLimitAlgorithm> algorithms;

    public RateLimiter(Map<RateLimitAlgorithmType, RateLimitAlgorithm> algorithms) {
        this.algorithms = algorithms;
    }

    public RateLimitDecision check(RateLimitAlgorithmType type, String resolvedKey, int limit, int windowSeconds){
        RateLimitAlgorithm algorithm = algorithms.get(type);
        if(algorithm == null){
            throw new IllegalStateException("No algorithm registered for type: " + type);
        }

        return algorithm.isAllowed(resolvedKey, limit, windowSeconds);
    }
}