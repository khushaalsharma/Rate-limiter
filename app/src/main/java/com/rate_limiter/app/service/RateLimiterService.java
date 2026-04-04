package com.rate_limiter.app.service;

import com.rate_limiter.app.DTO.RateLimitEventDto;
import com.rate_limiter.app.models.RateLimitConfig;
import com.rate_limiter.app.repository.RateLimitConfigRepository;
import com.rate_limiter.app.service.algorithms.FixedWindowAlgorithm;
import com.rate_limiter.app.service.algorithms.RateLimitResult;
import com.rate_limiter.app.service.algorithms.SlidingWindowAlgorithm;
import com.rate_limiter.app.service.algorithms.TokenBucketAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimiterService {
    private final RateLimitConfigRepository rateLimitConfigRepository;
    private final SlidingWindowAlgorithm slidingWindowAlgorithm;
    private final FixedWindowAlgorithm fixedWindowAlgorithm;
    private final TokenBucketAlgorithm tokenBucketAlgorithm;
    private final KafkaTemplate<String, RateLimitEventDto> kafkaTemplate;

    private static final String RATE_LIMIT_TOPIC = "rate-limit-events";

    public RateLimitResult checkRateLimit(String clientKey, String endpoint) throws Exception {
        Optional<RateLimitConfig> config = rateLimitConfigRepository.findByClientKey(clientKey);

        if(config.isPresent()){
            RateLimitResult result = runAlgorithm(config.get());

            publishEvent(clientKey, endpoint, result, config.get().getAlgorithmType().name());

            return result;

        }else{
            throw new Exception("no rate limit config found for client: " + clientKey);
        }
    }

    private RateLimitResult runAlgorithm(RateLimitConfig config){
        return switch(config.getAlgorithmType()){
            case FIXED_WINDOW -> fixedWindowAlgorithm.isAllowed(config.getClientKey(), config.getMaxRequests(), config.getWindowSeconds());
            case SLIDING_WINDOW -> slidingWindowAlgorithm.isAllowed(config.getClientKey(), config.getMaxRequests(), config.getWindowSeconds());
            case TOKEN_BUCKET -> tokenBucketAlgorithm.isAllowed(config.getClientKey(), config.getMaxRequests(), config.getWindowSeconds());
        };
    }

    private void publishEvent(String clientKey, String endpoint, RateLimitResult result, String algorithm){
        RateLimitEventDto event = RateLimitEventDto.builder()
                .clientKey(clientKey)
                .endpoint(endpoint)
                .allowed(result.isAllowed())
                .remainingRequests(result.getRemainingRequests())
                .algorithm(algorithm)
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send(RATE_LIMIT_TOPIC, clientKey, event)
                .whenComplete((sendResult, ex) -> {
                    if(ex != null){
                        log.error("failed to publish rate limit event for key={}", clientKey);
                    }else{
                        log.debug("Published event for key={} allowed={}", clientKey, result.isAllowed());
                    }
                });
    }
}
