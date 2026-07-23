package com.rate_limiter.app.DTO;

import com.rate_limiter.app.models.RateLimitConfig;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class RateLimitConfigRequest {
    private String clientKey;

    @NotBlank
    @Min(value = 10)
    private int maxRequests;

    @NotBlank
    @Min(value = 1)
    private int windowSeconds;

    @NotNull
    private RateLimitConfig.AlgorithmType algorithm;   
}
