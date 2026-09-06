package io.github.khushaalsharma.ratelimiter.core;

public interface RateLimitKeyResolver<T> {
    RateLimitKey resolve(T context);
}