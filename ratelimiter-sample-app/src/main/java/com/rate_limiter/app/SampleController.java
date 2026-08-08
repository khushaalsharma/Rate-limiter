package com.rate_limiter.app;

import io.github.khushaalsharma.ratelimiter.autoconfigure.annotation.RateLimit;
import io.github.khushaalsharma.ratelimiter.autoconfigure.annotation.RateLimitStrategy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SampleController {

    @GetMapping("/orders")
    @RateLimit(key = "orders", limit = 3, window = "1m", strategy = RateLimitStrategy.PATH)
    public String getOrders(){
        return "orders";
    }

    @GetMapping("/profile")
    @RateLimit(key = "profile", limit = 3, window = "1m", strategy = RateLimitStrategy.PATH)
    public String getProfile() {
        return "user profile";
    }

    @GetMapping("/domain-test")
    @RateLimit(limit = 5, window = "1m", strategy = RateLimitStrategy.DOMAIN)
    public String domainTest() {
        return "shared bucket across all DOMAIN-strategy endpoints";
    }
}
