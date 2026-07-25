package io.github.khushaalsharma.ratelimiter.autoconfigure;

import io.github.khushaalsharma.ratelimiter.autoconfigure.analytics.NoOpRateLimitEventPublisher;
import io.github.khushaalsharma.ratelimiter.autoconfigure.analytics.RateLimitEventPublisher;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class RateLimiterAnalyticsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(RateLimitEventPublisher.class)
    public RateLimitEventPublisher noOpRateLimitEventPublisher(){
        return new NoOpRateLimitEventPublisher();
    }
}
