package com.rate_limiter.app.service.algorithms;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class FixedWindowAlgorithm implements RateLimitAlgorithm{

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public RateLimitResult isAllowed(String clientKey, int maxRequests, int windowSeconds) {
        long windowStart = Instant.now().getEpochSecond() / windowSeconds;
        String redisKey = "fl:fixed" + clientKey + ":" + windowStart;

        Long count = redisTemplate.opsForValue().increment(redisKey);

        //Set TTL(Time to live) only on first request for this window
        if(count == 1){
            redisTemplate.expire(redisKey, windowSeconds, TimeUnit.SECONDS);
        }

        long resetAfter = windowSeconds - (Instant.now().getEpochSecond() % windowSeconds);

        if(count > maxRequests){
            log.debug("FIXED window DENY - key={} count={} max={}", clientKey, count, maxRequests);
            return RateLimitResult.denied(resetAfter);
        }

        int remaining = (int) (maxRequests - count);
        log.debug("FIXED WINDOW ALLOWED - key={} count={} remaining={}", redisKey, count, remaining);

        return RateLimitResult.allowed(remaining, resetAfter);
    }
}
