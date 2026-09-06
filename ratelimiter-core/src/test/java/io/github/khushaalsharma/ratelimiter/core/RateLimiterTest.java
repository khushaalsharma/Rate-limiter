package io.github.khushaalsharma.ratelimiter.core;

import org.junit.jupiter.api.Test;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class RateLimiterTest {

    @Test
    void throwsWhenAlgorithmNotRegistered() {
        RateLimiter rateLimiter = new RateLimiter(Map.of());

        assertThatThrownBy(() ->
                rateLimiter.check(RateLimitAlgorithmType.TOKEN_BUCKET, "key", 10, 60)
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void delegatesToCorrectAlgorithm() {
        RateLimitAlgorithm mockAlgorithm = new RateLimitAlgorithm() {
            @Override
            public RateLimitDecision isAllowed(String resolvedKey, int maxRequests, int windowSeconds) {
                return new RateLimitDecision(true, 5, 60);
            }

            @Override
            public RateLimitAlgorithmType type() {
                return RateLimitAlgorithmType.TOKEN_BUCKET;
            }
        };

        RateLimiter rateLimiter = new RateLimiter(
                Map.of(RateLimitAlgorithmType.TOKEN_BUCKET, mockAlgorithm)
        );

        RateLimitDecision result = rateLimiter.check(RateLimitAlgorithmType.TOKEN_BUCKET, "key", 10, 60);
        assertThat(result.allowed()).isTrue();
    }
}