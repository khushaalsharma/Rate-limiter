package io.github.khushaalsharma.ratelimiter.autoconfigure.annotation;

import io.github.khushaalsharma.ratelimiter.core.RateLimitAlgorithmType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    String key() default "";

    int limit();

    String window();

    RateLimitStrategy strategy() default RateLimitStrategy.DEFAULT;

    RateLimitAlgorithmType algorithm() default RateLimitAlgorithmType.SLIDING_WINDOW;
}
