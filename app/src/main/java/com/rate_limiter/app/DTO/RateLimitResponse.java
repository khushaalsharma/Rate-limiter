package com.rate_limiter.app.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class RateLimitResponse {
    private boolean allowed;
    private int remainingRequests;
    private long resetAfterSeconds;    // when the window resets
    private String algorithm;
    private String message;
}
