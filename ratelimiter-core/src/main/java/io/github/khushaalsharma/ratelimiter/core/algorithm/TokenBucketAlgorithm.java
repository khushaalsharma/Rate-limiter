package io.github.khushaalsharma.ratelimiter.core.algorithm;

import io.github.khushaalsharma.ratelimiter.core.RateLimitAlgorithm;
import io.github.khushaalsharma.ratelimiter.core.RateLimitAlgorithmType;
import io.github.khushaalsharma.ratelimiter.core.RateLimitDecision;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.List;

public class TokenBucketAlgorithm implements RateLimitAlgorithm{

    private final RedisTemplate<String, String> redisTemplate;
    private final DefaultRedisScript<List> script;

    public TokenBucketAlgorithm(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.script = new DefaultRedisScript<>();
        this.script.setResultType(List.class);
        this.script.setLocation(new ClassPathResource("scripts/token_bucket.lua"));
    }

    @Override
    public RateLimitDecision isAllowed(String resolvedKey, int maxRequests, int windowSeconds) {
        String redisKey = "rl:token:" + resolvedKey;
        double refillRate = (double) maxRequests / windowSeconds;
        long now = System.currentTimeMillis() / 1000;

        List<Long> result = redisTemplate.execute(
                script,
                Collections.singletonList(redisKey),
                String.valueOf(maxRequests),
                String.valueOf(refillRate),
                String.valueOf(now)
        );

        if (result == null || result.isEmpty()) {
            // Redis error — fail open (allow the request)
            return new RateLimitDecision(true, maxRequests, windowSeconds);
        }

        boolean allowed = result.get(0) == 1L;
        int remaining = result.get(1).intValue();
        long resetAfter = result.get(2);

        if (allowed) {
            return new RateLimitDecision(true, remaining, resetAfter);
        } else {
            return new RateLimitDecision(false, 0, resetAfter);
        }
    }

    @Override
    public RateLimitAlgorithmType type() {
        return RateLimitAlgorithmType.TOKEN_BUCKET;
    }
}