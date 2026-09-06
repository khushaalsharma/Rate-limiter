package io.github.khushaalsharma.ratelimiter.core.redis;

public class RedisRateLimitProperties {
    private final String keyPrefix;
    private final int scriptTimeoutMs;

    public RedisRateLimitProperties(String keyPrefix, int scriptTimeoutMs) {
        this.keyPrefix = keyPrefix;
        this.scriptTimeoutMs = scriptTimeoutMs;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public int getScriptTimeoutMs() {
        return scriptTimeoutMs;
    }
}