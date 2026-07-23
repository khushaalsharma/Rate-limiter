package com.rate_limiter.app.service.algorithms;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RateLimitResult {
    private boolean isAllowed;
    private int remainingRequests;
    private long resetAfterSeconds;

    public static RateLimitResult allowed(int remaining, long resetAfter){
        return RateLimitResult.builder()
                .isAllowed(true)
                .remainingRequests(remaining)
                .resetAfterSeconds(resetAfter)
                .build();
    }

    public static RateLimitResult denied(long resetAfter){
        return RateLimitResult.builder()
                .isAllowed(false)
                .remainingRequests(0)
                .resetAfterSeconds(resetAfter)
                .build();
    }
}
