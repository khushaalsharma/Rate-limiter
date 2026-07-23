package com.rate_limiter.app.service.algorithms;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Token Bucket algorithm.
 *
 * How it works:
 *   - Each client has a "bucket" with capacity = maxRequests tokens
 *   - Tokens refill at rate = maxRequests / windowSeconds per second
 *   - Each request consumes 1 token
 *   - If bucket is empty → reject; otherwise allow and decrement
 *
 * Redis stores: {tokens (float), lastRefillTimestamp (epoch seconds)}
 * Redis key: rl:token:{clientKey}
 *
 * WHY LUA SCRIPT?
 *   The check-refill-decrement sequence must be atomic. Without atomicity,
 *   two concurrent requests could both read tokens=1, both pass the check,
 *   and both decrement — resulting in tokens=-1 (race condition).
 *   Redis executes Lua scripts atomically, so this is the correct solution.
 *
 *
 * Advantage: allows controlled bursting — a client can burst up to maxRequests
 * instantly, then sustained rate is capped. More forgiving than window-based.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenBucketAlgorithm implements RateLimitAlgorithm {

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * Lua script executed atomically on Redis.
     *
     * KEYS[1] = Redis key for this client
     * ARGV[1] = maxRequests (bucket capacity)
     * ARGV[2] = refill rate (tokens per second = maxRequests / windowSeconds)
     * ARGV[3] = current timestamp (epoch seconds)
     *
     * Returns: {allowed (1/0), remaining_tokens (int), reset_after_seconds (int)}
     */
    private static final String TOKEN_BUCKET_SCRIPT = """
            local key = KEYS[1]
            local capacity = tonumber(ARGV[1])
            local refill_rate = tonumber(ARGV[2])
            local now = tonumber(ARGV[3])
            
            local bucket = redis.call('HMGET', key, 'tokens', 'last_refill')
            local tokens = tonumber(bucket[1])
            local last_refill = tonumber(bucket[2])
            
            -- First request for this client: initialize full bucket
            if tokens == nil then
                tokens = capacity
                last_refill = now
            end
            
            -- Refill tokens based on time elapsed since last refill
            local elapsed = now - last_refill
            local refilled = elapsed * refill_rate
            tokens = math.min(capacity, tokens + refilled)
            last_refill = now
            
            local reset_after = math.ceil((1 - (tokens % 1)) / refill_rate)
            
            if tokens >= 1 then
                tokens = tokens - 1
                redis.call('HMSET', key, 'tokens', tokens, 'last_refill', last_refill)
                redis.call('EXPIRE', key, math.ceil(capacity / refill_rate) + 60)
                return {1, math.floor(tokens), reset_after}
            else
                redis.call('HMSET', key, 'tokens', tokens, 'last_refill', last_refill)
                redis.call('EXPIRE', key, math.ceil(capacity / refill_rate) + 60)
                return {0, 0, reset_after}
            end
            """;

    private final DefaultRedisScript<List> redisScript = new DefaultRedisScript<>(TOKEN_BUCKET_SCRIPT, List.class);

    @Override
    public RateLimitResult isAllowed(String clientKey, int maxRequests, int windowSeconds) {
        String redisKey = "rl:token:" + clientKey;
        double refillRate = (double) maxRequests / windowSeconds;
        long now = System.currentTimeMillis() / 1000;

        List<Long> result = redisTemplate.execute(
                redisScript,
                Collections.singletonList(redisKey),
                String.valueOf(maxRequests),
                String.valueOf(refillRate),
                String.valueOf(now)
        );

        if (result == null || result.isEmpty()) {
            // Redis error — fail open (allow the request)
            log.error("Token bucket Lua script returned null for key={}", clientKey);
            return RateLimitResult.allowed(maxRequests, windowSeconds);
        }

        boolean allowed = result.get(0) == 1L;
        int remaining = result.get(1).intValue();
        long resetAfter = result.get(2);

        if (allowed) {
            log.debug("Token bucket ALLOW — key={} remaining={}", clientKey, remaining);
            return RateLimitResult.allowed(remaining, resetAfter);
        } else {
            log.debug("Token bucket DENY — key={}", clientKey);
            return RateLimitResult.denied(resetAfter);
        }
    }
}