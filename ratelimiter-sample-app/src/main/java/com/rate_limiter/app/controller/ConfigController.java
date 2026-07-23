package com.rate_limiter.app.controller;

import com.rate_limiter.app.DTO.RateLimitConfigRequest;
import com.rate_limiter.app.models.RateLimitConfig;
import com.rate_limiter.app.service.ConfigService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/configs")
@RequiredArgsConstructor
public class ConfigController {

    private final ConfigService configService;

    @PostMapping
    public ResponseEntity<RateLimitConfig> createConfig(@Valid @RequestBody RateLimitConfigRequest body){
        RateLimitConfig config = configService.createConfig(
                body.getClientKey(),
                body.getMaxRequests(),
                body.getWindowSeconds(),
                body.getAlgorithm()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(config);
    }

    @GetMapping
    public ResponseEntity<List<RateLimitConfig>> getAllConfigs() {
        return ResponseEntity.ok(configService.getAllConfigs());
    }

    @GetMapping("/{clientKey}")
    public ResponseEntity<RateLimitConfig> getConfig(@PathVariable String clientKey) {
        return ResponseEntity.ok(configService.getConfig(clientKey));
    }

    @PutMapping("/{clientKey}")
    public ResponseEntity<RateLimitConfig> updateConfig(@PathVariable String clientKey, @RequestBody Map<String, String> body){
        RateLimitConfig updated = configService.updateConfig(
                clientKey,
                Integer.parseInt(body.get("maxRequests")),
                Integer.parseInt(body.get("windowSeconds")),
                RateLimitConfig.AlgorithmType.valueOf(body.get("algorithm"))
        );
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{clientKey}")
    public ResponseEntity<Void> deleteConfig(@PathVariable String clientKey) {
        configService.deleteConfig(clientKey);
        return ResponseEntity.noContent().build();
    }

}
