package com.rate_limiter.app.controller;

import com.rate_limiter.app.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    @GetMapping("/{clientKey}")
    public ResponseEntity<Map<String, Object>> getStats(
            @PathVariable String clientKey,
            @RequestParam(defaultValue = "60") int minutes) {
        return ResponseEntity.ok(analyticsService.getStats(clientKey, minutes));
    }
}
