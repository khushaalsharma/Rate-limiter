package io.github.khushaalsharma.ratelimiter.core.algorithm;

import io.github.khushaalsharma.ratelimiter.core.RateLimitAlgorithm;
import io.github.khushaalsharma.ratelimiter.core.RateLimitAlgorithmType;
import io.github.khushaalsharma.ratelimiter.core.RateLimitDecision;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class FixedWindowAlgorithm implements RateLimitAlgorithm{

    private final RedisTemplate<String, String> template;

    public FixedWindowAlgorithm(RedisTemplate<String, String> template) {
        this.template = template;
    }

    @Override
    public RateLimitDecision isAllowed(String resolvedKey, int maxRequests, int windowSeconds) {
        long windowStart = Instant.now().getEpochSecond() / windowSeconds;
        String redisKey = "rl:fixed" + resolvedKey + ":" + windowStart;

        Long count = template.opsForValue().increment(redisKey);

        if (count == 1) {
            template.expire(redisKey, windowSeconds, TimeUnit.SECONDS);
        }

        long resetAfter = windowSeconds - (Instant.now().getEpochSecond() % windowSeconds);

        if (count > maxRequests) {
            return new RateLimitDecision(false, maxRequests - count, resetAfter);
        } else {
            return new RateLimitDecision(true, maxRequests - count, resetAfter);
        }
    }

    @Override
    public RateLimitAlgorithmType type() {
        return RateLimitAlgorithmType.FIXED_WINDOW;
    }
}