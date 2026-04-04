package com.rate_limiter.app.config;

import com.rate_limiter.app.DTO.RateLimitEventDto;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class AppConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    // ─── Redis ───────────────────────────────────────────────────────────────

    /**
     * RedisTemplate with String serializers for both key and value.
     * All three algorithms use this template directly.
     */
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory factory) {
        /*
        * Redis Template is the Redis client, in Redis we store everything in bytes,
        * Since our template here is of String, String thus we require serializer and deserializers
        * to send and receive data from redis cache
        * */

        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(factory); //from the properties file, establishes the connection
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet(); //manually triggers spring's initialization cycle, without this serializers don't inject
        return template;
    }

    // ─── Kafka Producer ───────────────────────────────────────────────────────

    //Kafka broker contains all the topics, partitions, handles I/O acting as a storage layer
    @Bean
    public ProducerFactory<String, RateLimitEventDto> producerFactory() { //creates kafka producer instances.
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers); //connection with kafka from prop file
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);//need to send data in bytes
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class); //byte array as object
        props.put(ProducerConfig.ACKS_CONFIG, "1"); //after sending a message, how many kafka broker ACK do you wait for considering it sent can take values "1" or "all"
        props.put(ProducerConfig.RETRIES_CONFIG, 3); //if the broker is down retry attempts to send message to broker
        // Tell JsonSerializer not to embed type headers in the message —
        // keeps messages clean and avoids type-mismatch issues on the consumer side
        //This is meant to help the consumer know what class to deserialize into. We turn it off
        // because we tell the consumer explicitly via VALUE_DEFAULT_TYPE — no need for the header,
        // and it keeps messages clean.
        props.put(JacksonJsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaProducerFactory<>(props); //standard implementation of producer factory in spring
    }

    @Bean
    public KafkaTemplate<String, RateLimitEventDto> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    // ─── Kafka Consumer ───────────────────────────────────────────────────────

    @Bean
    public ConsumerFactory<String, RateLimitEventDto> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        /*
        * this is one of Kafka's most important concepts. A consumer group is a set of consumers that
        * collectively consume a topic. Kafka guarantees each partition is consumed by only one consumer
        * within the same group at a time. So if your topic has 3 partitions and you have 3 consumer threads,
        * each thread handles one partition — full parallelism. If two different apps both need the same events,
        * they use different group IDs and each gets all the messages independently.
        * */
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "analytics-group");
        //so that new consumers don't miss old messages this helps in
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class);
        // Tell JsonDeserializer exactly which class to deserialize into —
        // no deprecated Class<T> constructor needed, fully property-driven
        props.put(JacksonJsonDeserializer.VALUE_DEFAULT_TYPE, RateLimitEventDto.class.getName());
        // Trust our own package; reject anything else for security
        props.put(JacksonJsonDeserializer.TRUSTED_PACKAGES, "com.rate_limiter.dto");
        // Since producer sets ADD_TYPE_INFO_HEADERS=false, tell consumer
        // not to look for type headers and use VALUE_DEFAULT_TYPE instead
        props.put(JacksonJsonDeserializer.USE_TYPE_INFO_HEADERS, false);

        return new DefaultKafkaConsumerFactory<>(props);
    }

    /*
    * ConcurrentKafkaListenerContainerFactory this is the bridge between the Kafka consumer and the
    * @KafkaListener annotation, Spring find a bean of this type on encountering the annotation
    * and uses it to create a listener container
    * */

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, RateLimitEventDto>
    kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, RateLimitEventDto> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(3); // 3 consumer threads in parallel ie each group has three consumer and works ideally if topics are 3+
        return factory;
    }
}