package io.github.khushaalsharma.ratelimiter.autoconfigure.annotation;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

public class RateLimitingConfigSelector implements ImportSelector {

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[] {
                "io.github.khushaalsharma.ratelimiter.autoconfigure.RateLimiterRedisAutoConfiguration",
                "io.github.khushaalsharma.ratelimiter.autoconfigure.RateLimiterAopAutoConfiguration",
                "io.github.khushaalsharma.ratelimiter.autoconfigure.RateLimiterAnalyticsAutoConfiguration",
                "io.github.khushaalsharma.ratelimiter.autoconfigure.RateLimiterKafkaAnalyticsAutoConfiguration",
                "io.github.khushaalsharma.ratelimiter.autoconfigure.RateLimiterJpaAnalyticsAutoConfiguration"
        };
    }
}
