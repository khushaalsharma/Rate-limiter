package com.rate_limiter.app.service.algorithms;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

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
    /*
        redisKey,
        nowMillis,
        maxRequests,
        windowSeconds,
        currentThreadId
    */
    private final String SLIDING_WINDOW_SCRIPT = """
                local key = KEYS[1]
                local nowMillis = tonumber(ARGV[1])
                local maxRequests = tonumber(ARGV[2])
                local windowSeconds = tonumber(ARGV[3])
                local memberSuffix = ARGV[4]

                local windowStart = nowMillis - (windowSeconds * 1000)

                redis.call('ZREMRANGEBYSCORE', key, 0, windowStart)

                local count = redis.call('ZCARD', key)

                if count < maxRequests then
                    local member = tostring(nowMillis) .. "-" .. memberSuffix
                    redis.call('ZADD', key, nowMillis, member)
                    redis.call('EXPIRE', key, windowSeconds * 2)
                    return {1, maxRequests - count - 1, windowSeconds}
                else
                    redis.call('EXPIRE', key, windowSeconds * 2)
                    return {0, 0, windowSeconds}
                end
            """;

    private final DefaultRedisScript<List> redisScript = new DefaultRedisScript<>(SLIDING_WINDOW_SCRIPT, List.class);
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public RateLimitResult isAllowed(String clientKey, int maxRequests, int windowSeconds) {
        String redisKey = "rl:sliding:" + clientKey;
        long nowMillis = Instant.now().toEpochMilli();

        List<Long> currentCount = redisTemplate.execute(
            redisScript,
            Collections.singletonList(redisKey),
            nowMillis,
            maxRequests,
            windowSeconds   
        );

        long resetAfter = currentCount.get(2);

        if(currentCount.get(0) != 1L){
            log.debug("SLIDING WINDOW DENIED - key={} count={} max={}", redisKey, currentCount, maxRequests);
            return RateLimitResult.denied(resetAfter);
        }

        int remaining = currentCount.get(1).intValue();
        log.debug("SLIDING WINDOW ALLOWED - key={} count={} remaining={}", redisKey, currentCount, remaining);

        return RateLimitResult.allowed(remaining, resetAfter);
    }
}
