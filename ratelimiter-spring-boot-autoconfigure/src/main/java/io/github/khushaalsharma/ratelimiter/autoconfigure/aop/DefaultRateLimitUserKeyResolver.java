package io.github.khushaalsharma.ratelimiter.autoconfigure.aop;

import jakarta.servlet.http.HttpServletRequest;

public class DefaultRateLimitUserKeyResolver implements RateLimitUserKeyResolver{

    public static final String DEFAULT_HEADER = "X-RateLimit-Client-Id";

    @Override
    public String resolve(HttpServletRequest request) {
        String clientId = request.getHeader(DEFAULT_HEADER);
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalStateException(
                    "Missing required header '" + DEFAULT_HEADER + "' for rate limiting. " +
                            "Provide this header, or register a custom RateLimitUserKeyResolver bean."
            );
        }
        return clientId;
    }
}
