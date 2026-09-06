package io.github.khushaalsharma.ratelimiter.autoconfigure.annotation;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(RateLimitingConfigSelector.class)
public @interface EnableRateLimiting {
}
