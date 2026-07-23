package io.github.khushaalsharma.ratelimiter.core.algorithm;

import io.github.khushaalsharma.ratelimiter.core.AbstractRedisIntegrationTest;
import io.github.khushaalsharma.ratelimiter.core.RateLimitDecision;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TokenBucketAlgorithmTest extends AbstractRedisIntegrationTest {

    private final TokenBucketAlgorithm algorithm = new TokenBucketAlgorithm(redisTemplate);

    @Test
    void allowsRequestsWithinLimit() {
        RateLimitDecision decision = algorithm.isAllowed("test-key-1", 5, 60);

        assertThat(decision.allowed()).isTrue();
        assertThat(decision.remainingRequests()).isEqualTo(4);
    }

    @Test
    void rejectsRequestsOverLimit() {
        String key = "test-key-2";
        for (int i = 0; i < 3; i++) {
            algorithm.isAllowed(key, 3, 60);
        }

        RateLimitDecision decision = algorithm.isAllowed(key, 3, 60);

        assertThat(decision.allowed()).isFalse();
    }

    @Test
    void independentKeysDoNotAffectEachOther() {
        RateLimitDecision decisionA = algorithm.isAllowed("user-a", 1, 60);
        RateLimitDecision decisionB = algorithm.isAllowed("user-b", 1, 60);

        assertThat(decisionA.allowed()).isTrue();
        assertThat(decisionB.allowed()).isTrue();
    }
}