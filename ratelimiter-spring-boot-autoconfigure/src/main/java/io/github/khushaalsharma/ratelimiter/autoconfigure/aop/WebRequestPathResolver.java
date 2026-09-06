package io.github.khushaalsharma.ratelimiter.autoconfigure.aop;

import io.github.khushaalsharma.ratelimiter.autoconfigure.annotation.RateLimit;

public class WebRequestPathResolver {
    public String resolve(RateLimit rateLimit, String userId){
        String routeIdentifier = rateLimit.key().isBlank()
                ? "default"
                : rateLimit.key();
        return userId + ":path:" + routeIdentifier;
    }

    public String resolveDomain(String userId) {
        return userId;
    }
}
