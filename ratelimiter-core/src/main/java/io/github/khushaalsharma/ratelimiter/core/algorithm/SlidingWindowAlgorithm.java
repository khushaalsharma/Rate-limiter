package io.github.khushaalsharma.ratelimiter.core.algorithm;

import io.github.khushaalsharma.ratelimiter.core.RateLimitAlgorithm;
import io.github.khushaalsharma.ratelimiter.core.RateLimitAlgorithmType;
import io.github.khushaalsharma.ratelimiter.core.RateLimitDecision;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.io.File;
import java.io.InputStream;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class SlidingWindowAlgorithm implements RateLimitAlgorithm{

    private final RedisTemplate<String, String> template;
    private final DefaultRedisScript<List> redisScript;

    public SlidingWindowAlgorithm(RedisTemplate<String, String> template) {
        this.template = template;
        this.redisScript = new DefaultRedisScript<>();
        this.redisScript.setLocation(new ClassPathResource("scripts/sliding_window.lua"));
        this.redisScript.setResultType(List.class);
    }

    @Override
    public RateLimitDecision isAllowed(String resolvedKey, int maxRequests, int windowSeconds) {
        String redisKey = "rl:sliding:" + resolvedKey;
        long nowMillis = Instant.now().toEpochMilli();

        List<Long> currentCount = template.execute(
                redisScript,
                Collections.singletonList(redisKey),
                String.valueOf(nowMillis),
                String.valueOf(maxRequests),
                String.valueOf(windowSeconds),
                UUID.randomUUID().toString()
        );

        System.out.println(">>>> In SLIDING WINDOW isAllowed:");
        for(Long val : currentCount){
            System.out.println(val);
        }

        long resetAfter = currentCount.get(2);

        if(currentCount.get(0) == 1L){
            //log.debug("SLIDING WINDOW DENIED - key={} count={} max={}", redisKey, currentCount, maxRequests);
            return new RateLimitDecision(true, 0, resetAfter);
        }

        int remaining = currentCount.get(1).intValue();
        //log.debug("SLIDING WINDOW ALLOWED - key={} count={} remaining={}", redisKey, currentCount, remaining);

        return new RateLimitDecision(false, remaining, resetAfter);
    }

    @Override
    public RateLimitAlgorithmType type() {
        return RateLimitAlgorithmType.SLIDING_WINDOW;
    }
}

