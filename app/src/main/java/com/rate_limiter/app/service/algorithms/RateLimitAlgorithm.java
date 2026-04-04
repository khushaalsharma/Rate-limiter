package com.rate_limiter.app.service.algorithms;

public interface RateLimitAlgorithm {
    RateLimitResult isAllowed(String clientKey, int maxRequests, int windowSeconds);
}
