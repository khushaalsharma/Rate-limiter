package io.github.khushaalsharma.ratelimiter.autoconfigure;

import io.github.khushaalsharma.ratelimiter.autoconfigure.analytics.KafkaRateLimitEventPublisher;
import io.github.khushaalsharma.ratelimiter.autoconfigure.analytics.RateLimitEventDto;
import io.github.khushaalsharma.ratelimiter.autoconfigure.analytics.RateLimitEventPublisher;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;

@AutoConfiguration(before = RateLimiterAnalyticsAutoConfiguration.class, after = KafkaAutoConfiguration.class)
@ConditionalOnBean(KafkaTemplate.class)
@ConditionalOnClass(KafkaTemplate.class)
@ConditionalOnProperty(prefix = "ratelimiter.analytics", name = "enabled", havingValue = "true")
public class RateLimiterKafkaAnalyticsAutoConfiguration {

    @Bean
    public RateLimitEventPublisher kafkaRateLimitEventPublisher(
        KafkaTemplate<String, RateLimitEventDto> kafkaTemplate,
        RateLimiterProperties properties
    ){
        return new KafkaRateLimitEventPublisher(kafkaTemplate, properties.getAnalytics().getTopic(), properties.getAnalytics().getGroupId());
    }
}
