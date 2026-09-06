package io.github.khushaalsharma.ratelimiter.core;

public interface RateLimitAlgorithm {
    RateLimitDecision isAllowed(String resolvedKey, int maxRequests, int windowSeconds);
    RateLimitAlgorithmType type();
}