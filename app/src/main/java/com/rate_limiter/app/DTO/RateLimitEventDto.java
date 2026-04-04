package com.rate_limiter.app.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateLimitEventDto {
    private String clientKey;
    private String endpoint;
    private boolean allowed;
    private String algorithm;
    private int remainingRequests;
    private LocalDateTime timestamp;
}
