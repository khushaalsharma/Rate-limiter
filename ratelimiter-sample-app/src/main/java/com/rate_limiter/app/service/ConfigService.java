package com.rate_limiter.app.service;

import com.rate_limiter.app.models.RateLimitConfig;
import com.rate_limiter.app.repository.RateLimitConfigRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigService {
    private final RateLimitConfigRepository configRepository;

    @CachePut(value = "rateLimitConfigs", key = "#clientKey")
    public RateLimitConfig createConfig(String clientKey, int maxRequests, int windowSeconds, RateLimitConfig.AlgorithmType algorithm){
        if(configRepository.existsByClientKey(clientKey)){
            throw new IllegalArgumentException("config already exists for client: " + clientKey);
        }

        RateLimitConfig config = RateLimitConfig.builder()
                .clientKey(clientKey)
                .maxRequests(maxRequests)
                .windowSeconds(windowSeconds)
                .algorithmType(algorithm)
                .build();

        RateLimitConfig saved = configRepository.save(config);
        log.info("Created rate limit config clientKey={} algorithm={}", clientKey, algorithm);
        return saved;
    }

    @Cacheable(value = "rateLimitConfigs", key = "#clientKey")
    public RateLimitConfig getConfig(String clientKey){
        return configRepository.findByClientKey(clientKey)
                .orElseThrow(() -> new IllegalArgumentException("Client not found: " + clientKey));
    }

    public List<RateLimitConfig> getAllConfigs(){
        return configRepository.findAll();
    }

    @CachePut(value = "rateLimitConfigs", key = "#clientKey")
    @Transactional
    public RateLimitConfig updateConfig(String clientKey, int maxRequests,
                                        int windowSeconds, RateLimitConfig.AlgorithmType algorithm) {
        RateLimitConfig config = getConfig(clientKey);
        config.setMaxRequests(maxRequests);
        config.setWindowSeconds(windowSeconds);
        config.setAlgorithmType(algorithm);
        return configRepository.save(config);
    }

    @CacheEvict(value = "rateLimitConfigs", key = "#clientKey")
    @Transactional
    public void deleteConfig(String clientKey) {
        if (!configRepository.existsByClientKey(clientKey)) {
            throw new IllegalArgumentException("Client not found: " + clientKey);
        }
        configRepository.deleteByClientKey(clientKey);
        log.info("Deleted rate limit config for clientKey={}", clientKey);
    }
}
