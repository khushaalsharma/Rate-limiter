package com.rate_limiter.app.controller;

import com.rate_limiter.app.service.RateLimiterService;
import com.rate_limiter.app.service.algorithms.RateLimitResult;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.rate_limiter.app.DTO.RateLimitRequest;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/rate-limit")
@RequiredArgsConstructor
public class RateLimitController {
    private final RateLimiterService rateLimiterService;

    @PostMapping("/check")
    public ResponseEntity<Map<String, Object>> checkRateLimit(@Valid @RequestBody RateLimitRequest request) throws Exception {
        String clientKey = request.getClientKey();
        String endpoint = request.getEndpoint() != null ? request.getEndpoint() : "unknown" ;

        RateLimitResult result = rateLimiterService.checkRateLimit(clientKey, endpoint);

        Map<String, Object> response = Map.of(
                "allowed", result.isAllowed(),
                "remainingRequests", result.getRemainingRequests(),
                "resetAfterSeconds", result.getResetAfterSeconds(),
                "clientKey", clientKey
        );

        HttpStatus status = result.isAllowed() ? HttpStatus.OK : HttpStatus.TOO_MANY_REQUESTS;
        return ResponseEntity.status(status).body(response);

    }
}
