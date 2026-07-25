package io.github.khushaalsharma.ratelimiter.autoconfigure;

import io.github.khushaalsharma.ratelimiter.autoconfigure.analytics.RateLimitEvent;
import io.github.khushaalsharma.ratelimiter.autoconfigure.analytics.RateLimitEventConsumer;
import io.github.khushaalsharma.ratelimiter.autoconfigure.analytics.RateLimitEventRepository;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.sql.DataSource;

@AutoConfiguration(after = RateLimiterKafkaAnalyticsAutoConfiguration.class)
@ConditionalOnClass({LocalContainerEntityManagerFactoryBean.class})
@ConditionalOnBean({KafkaTemplate.class, DataSource.class})
@ConditionalOnProperty(prefix = "ratelimiter.analytics", name = "enabled", havingValue = "true")
@EnableJpaRepositories(basePackageClasses = RateLimitEventRepository.class)
@EntityScan(basePackageClasses = RateLimitEvent.class)
public class RateLimiterJpaAnalyticsAutoConfiguration {

    @Bean
    public RateLimitEventConsumer rateLimitEventConsumer(RateLimitEventRepository repository){
        return new RateLimitEventConsumer(repository);
    }
}
