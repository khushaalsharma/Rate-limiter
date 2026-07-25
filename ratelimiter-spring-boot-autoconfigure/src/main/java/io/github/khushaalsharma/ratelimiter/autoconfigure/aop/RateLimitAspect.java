package io.github.khushaalsharma.ratelimiter.autoconfigure.aop;

import io.github.khushaalsharma.ratelimiter.autoconfigure.RateLimiterProperties;
import io.github.khushaalsharma.ratelimiter.autoconfigure.analytics.RateLimitEventConsumer;
import io.github.khushaalsharma.ratelimiter.autoconfigure.analytics.RateLimitEventDto;
import io.github.khushaalsharma.ratelimiter.autoconfigure.analytics.RateLimitEventPublisher;
import io.github.khushaalsharma.ratelimiter.autoconfigure.annotation.RateLimit;
import io.github.khushaalsharma.ratelimiter.autoconfigure.annotation.RateLimitStrategy;
import io.github.khushaalsharma.ratelimiter.core.RateLimitAlgorithmType;
import io.github.khushaalsharma.ratelimiter.core.RateLimitDecision;
import io.github.khushaalsharma.ratelimiter.core.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class RateLimitAspect {
    private final RateLimiter rateLimiter;
    private final RateLimiterProperties properties;
    private final RateLimitUserKeyResolver keyResolver;
    private final WebRequestPathResolver pathResolver;
    private final RateLimitEventPublisher eventPublisher;

    public RateLimitAspect(RateLimiter rateLimiter,
                           RateLimiterProperties properties,
                           RateLimitUserKeyResolver keyResolver,
                           WebRequestPathResolver pathResolver, RateLimitEventPublisher eventPublisher
    ) {
        this.rateLimiter = rateLimiter;
        this.properties = properties;
        this.keyResolver = keyResolver;
        this.pathResolver = pathResolver;
        this.eventPublisher = eventPublisher;
    }

    @Around("@annotation(rateLimit)")
    public Object enforce(
            ProceedingJoinPoint pjp, RateLimit rateLimit
    ) throws Throwable{
        HttpServletRequest request = currentRequest();
        String userId = keyResolver.resolve(request);

        RateLimitStrategy strategy = rateLimit.strategy() == RateLimitStrategy.DEFAULT ?
                properties.getStrategy() : rateLimit.strategy();

        String identity = strategy == RateLimitStrategy.PATH
                ? pathResolver.resolve(rateLimit, userId)
                : pathResolver.resolveDomain(userId);

        RateLimitAlgorithmType algorithmType = rateLimit.algorithm() == RateLimitAlgorithmType.DEFAULT
                ? properties.getAlgorithm()
                : rateLimit.algorithm();

        int windowSeconds = (int) DurationStyle.detectAndParse(rateLimit.window()).toSeconds();

        RateLimitDecision decision = rateLimiter.check(
                algorithmType, identity, rateLimit.limit(), windowSeconds
        );

        if(!decision.allowed()){
            throw new RateLimitExceededException(decision);
        }

        eventPublisher.publish(new RateLimitEventDto(
                java.util.UUID.randomUUID().toString(),
                userId,
                rateLimit.key(),
                decision.allowed(),
                algorithmType.name(),
                decision.remainingRequests(),
                java.time.Instant.now()
        ));

        return pjp.proceed();
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();

        if(attrs == null){
            throw new IllegalStateException("RateLimitAspect can be used in a web request contract");
        }

        return attrs.getRequest();
    }
}
