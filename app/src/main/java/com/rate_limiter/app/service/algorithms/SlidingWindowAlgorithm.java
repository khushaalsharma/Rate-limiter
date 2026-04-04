package com.rate_limiter.app.service.algorithms;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * Sliding Window Log algorithm.
 *
 * How it works:
 *   - Uses a Redis Sorted Set where score = request timestamp (epoch millis)
 *   - On each request:
 *       1. Remove entries older than (now - windowSeconds)  → ZREMRANGEBYSCORE
 *       2. Count remaining entries                          → ZCARD
 *       3. If count < maxRequests, add current timestamp    → ZADD
 *       4. Set TTL on the key                               → EXPIRE
 *
 * Redis key: rl:sliding:{clientKey}
 *
 * Advantage over fixed window: no boundary burst — the window always
 * reflects exactly the last N seconds from *right now*.
 *
 * Trade-off: stores one entry per request (more memory than fixed window).
 * Acceptable for most use cases; can be bounded with maxRequests as cap.
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class SlidingWindowAlgorithm implements RateLimitAlgorithm{

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public RateLimitResult isAllowed(String clientKey, int maxRequests, int windowSeconds) {
        String redisKey = "rl:sliding:" + clientKey;
        long nowMillis = Instant.now().toEpochMilli();
        long windowStartMillis = nowMillis - (windowSeconds * 1000L);

        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();

        zSetOps.removeRangeByScore(redisKey, 0, windowStartMillis);

        Long count = zSetOps.zCard(redisKey);
        long currentCount = count == null ? 0 : count;

        long resetAfter = windowSeconds;

        if(currentCount >= maxRequests){
            log.debug("SLIDING WINDOW DENIED - key={} count={} max={}", redisKey, currentCount, maxRequests);
            return RateLimitResult.denied(resetAfter);
        }

        //add current request as unique member
        String member = nowMillis + "-" + Thread.currentThread().getId();
        zSetOps.add(redisKey, member, nowMillis);

        redisTemplate.expire(redisKey, windowSeconds * 2L, TimeUnit.SECONDS);

        int remaining = (int) (maxRequests - currentCount - 1);
        log.debug("SLIDING WINDOW ALLOWED - key={} count={} remaining={}", redisKey, currentCount, remaining);

        return RateLimitResult.allowed(remaining, resetAfter);
    }
}
