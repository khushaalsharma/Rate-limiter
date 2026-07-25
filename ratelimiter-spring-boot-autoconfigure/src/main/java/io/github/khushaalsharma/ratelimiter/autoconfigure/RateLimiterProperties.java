package io.github.khushaalsharma.ratelimiter.autoconfigure;

import io.github.khushaalsharma.ratelimiter.autoconfigure.annotation.RateLimitStrategy;
import io.github.khushaalsharma.ratelimiter.core.RateLimitAlgorithmType;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ratelimiter")
public class RateLimiterProperties {

    //default values for strategy and algorithm
    private RateLimitStrategy strategy = RateLimitStrategy.DOMAIN;
    private RateLimitAlgorithmType algorithm = RateLimitAlgorithmType.SLIDING_WINDOW;

    private Redis redis = new Redis();
    private Analytics analytics = new Analytics();

    public static class Redis {
        private String keyPrefix = "rl";

        public String getKeyPrefix() { return keyPrefix; }
        public void setKeyPrefix(String keyPrefix) { this.keyPrefix = keyPrefix; }
    }

    public static class Analytics {
        private boolean enabled = false;
        private String topic = "rate-limit-events";

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getTopic() { return topic; }
        public void setTopic(String topic) { this.topic = topic; }
    }

    // getters/setters for strategy, algorithm, redis, analytics
    public RateLimitStrategy getStrategy() { return strategy; }
    public void setStrategy(RateLimitStrategy strategy) { this.strategy = strategy; }
    public RateLimitAlgorithmType getAlgorithm() { return algorithm; }
    public void setAlgorithm(RateLimitAlgorithmType algorithm) { this.algorithm = algorithm; }
    public Redis getRedis() { return redis; }
    public void setRedis(Redis redis) { this.redis = redis; }
    public Analytics getAnalytics() { return analytics; }
    public void setAnalytics(Analytics analytics) { this.analytics = analytics; }
}
