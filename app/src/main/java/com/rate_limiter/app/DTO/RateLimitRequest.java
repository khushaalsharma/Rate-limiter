package com.rate_limiter.app.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class RateLimitRequest {

    @NotBlank(message = "clientKey can't empty")
    private String clientKey;   // IP address, API key, or user ID
    private String endpoint;    // which endpoint is being called
}
