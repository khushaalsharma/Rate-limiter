package io.github.khushaalsharma.ratelimiter.autoconfigure;

import io.github.khushaalsharma.ratelimiter.core.RateLimitAlgorithm;
import io.github.khushaalsharma.ratelimiter.core.RateLimitAlgorithmType;
import io.github.khushaalsharma.ratelimiter.core.RateLimiter;
import io.github.khushaalsharma.ratelimiter.core.algorithm.FixedWindowAlgorithm;
import io.github.khushaalsharma.ratelimiter.core.algorithm.SlidingWindowAlgorithm;
import io.github.khushaalsharma.ratelimiter.core.algorithm.TokenBucketAlgorithm;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.EnumMap;
import java.util.Map;

@AutoConfiguration
@ConditionalOnClass({RedisTemplate.class, RedisConnectionFactory.class})
public class RateLimiterRedisAutoConfiguration {

    @Bean
    public RateLimiterProperties rateLimiterProperties(){
        return new RateLimiterProperties();
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean
    public RedisTemplate<String, String> rateLimiterRedisTemplate(RedisConnectionFactory factory){
        RedisTemplate<String, String> template = new RedisTemplate<>();

        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();

        return template;
    }

    @Bean
    @ConditionalOnMissingBean
    public RateLimiter rateLimiter(RedisTemplate<String, String> redisTemplate){
        Map<RateLimitAlgorithmType, RateLimitAlgorithm> algorithms = new EnumMap<>(RateLimitAlgorithmType.class);
        algorithms.put(RateLimitAlgorithmType.FIXED_WINDOW, new FixedWindowAlgorithm(redisTemplate));
        algorithms.put(RateLimitAlgorithmType.SLIDING_WINDOW, new SlidingWindowAlgorithm(redisTemplate));
        algorithms.put(RateLimitAlgorithmType.TOKEN_BUCKET, new TokenBucketAlgorithm(redisTemplate));
        return new RateLimiter(algorithms);
    }
}
