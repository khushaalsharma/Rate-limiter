package io.github.khushaalsharma.ratelimiter.autoconfigure;

import io.github.khushaalsharma.ratelimiter.autoconfigure.analytics.RateLimitEvent;
import io.github.khushaalsharma.ratelimiter.autoconfigure.analytics.RateLimitEventConsumer;
import io.github.khushaalsharma.ratelimiter.autoconfigure.analytics.RateLimitEventRepository;
import org.apache.kafka.common.TopicPartition;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.util.backoff.FixedBackOff;

import javax.sql.DataSource;

@AutoConfiguration(after = {
        RateLimiterKafkaAnalyticsAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        KafkaAutoConfiguration.class
})
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

    @Bean
    public DefaultErrorHandler rateLimiterErrorHandler(KafkaOperations<Object, Object> kafkaOperations){
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaOperations,
                (record, ex) -> new TopicPartition(record.topic() + ".DLT", record.partition())
        );

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 3L));
        return errorHandler;
    }

    @Bean
    public org.springframework.beans.factory.config.BeanPostProcessor kafkaErrorHandlerAttacher(
            DefaultErrorHandler errorHandler
    ) {
        return new org.springframework.beans.factory.config.BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) {
                if (bean instanceof ConcurrentKafkaListenerContainerFactory<?, ?> factory) {
                    factory.setCommonErrorHandler(errorHandler);
                }
                return bean;
            }
        };
    }
}
