package com.rate_limiter.app.DTO;

import com.rate_limiter.app.models.RateLimitConfig;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

// DTO for rate limit check request
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class RateLimitRequest {
    private String clientKey;   // IP address, API key, or user ID
    private String endpoint;    // which endpoint is being called
}

// DTO returned on every request — allow or deny
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class RateLimitResponse {
    private boolean allowed;
    private int remainingRequests;
    private long resetAfterSeconds;    // when the window resets
    private String algorithm;
    private String message;
}

// DTO for creating/updating rate limit configs
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class RateLimitConfigRequest {
    private String clientKey;
    private int maxRequests;
    private int windowSeconds;
    private RateLimitConfig.AlgorithmType algorithm;
}

// DTO for analytics query response
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class AnalyticsResponse {
    private String clientKey;
    private long totalRequests;
    private long allowedRequests;
    private long rejectedRequests;
    private double rejectionRate;
    private LocalDateTime from;
    private LocalDateTime to;
}