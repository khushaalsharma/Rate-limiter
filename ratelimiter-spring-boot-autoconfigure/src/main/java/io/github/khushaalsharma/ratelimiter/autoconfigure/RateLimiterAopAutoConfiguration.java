package io.github.khushaalsharma.ratelimiter.autoconfigure;

import io.github.khushaalsharma.ratelimiter.autoconfigure.analytics.RateLimitEventPublisher;
import io.github.khushaalsharma.ratelimiter.autoconfigure.aop.DefaultRateLimitUserKeyResolver;
import io.github.khushaalsharma.ratelimiter.autoconfigure.aop.RateLimitAspect;
import io.github.khushaalsharma.ratelimiter.autoconfigure.aop.RateLimitUserKeyResolver;
import io.github.khushaalsharma.ratelimiter.autoconfigure.aop.WebRequestPathResolver;
import io.github.khushaalsharma.ratelimiter.core.RateLimitKeyResolver;
import io.github.khushaalsharma.ratelimiter.core.RateLimiter;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(after = RateLimiterRedisAutoConfiguration.class)
@ConditionalOnClass(Aspect.class)
@ConditionalOnBean(RateLimiter.class)
public class RateLimiterAopAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RateLimitUserKeyResolver rateLimitUserKeyResolver(){
        return new DefaultRateLimitUserKeyResolver();
    }

    @Bean
    public WebRequestPathResolver webRequestPathResolver(){
        return new WebRequestPathResolver();
    }

    @Bean
    public RateLimitAspect rateLimitAspect(
            RateLimiter rateLimiter,
            RateLimiterProperties properties,
            RateLimitUserKeyResolver keyResolver,
            WebRequestPathResolver pathResolver,
            RateLimitEventPublisher publisher
    ){
        return new RateLimitAspect(rateLimiter, properties, keyResolver, pathResolver, publisher);
    }
}
