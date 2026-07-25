package io.github.khushaalsharma.ratelimiter.autoconfigure.aop;

import jakarta.servlet.http.HttpServletRequest;

public interface RateLimitUserKeyResolver {
    String resolve(HttpServletRequest request);
}
