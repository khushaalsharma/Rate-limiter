package io.github.khushaalsharma.ratelimiter.core.redis;

public class RedisKeyBuilder{
    private final String prefix;

    public RedisKeyBuilder(String prefix) {
        this.prefix = prefix;
    }

    public String build(String algorithmName, String identity){
        return prefix + ":" + algorithmName + ":" + identity;
    }
}

