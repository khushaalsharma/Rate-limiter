package io.github.khushaalsharma.ratelimiter.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public abstract class AbstractRedisIntegrationTest {
    static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    protected static RedisTemplate<String, String> redisTemplate;

    @BeforeAll
    static void startContainerAndConnect(){
        REDIS.start();

        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(
                REDIS.getHost(), REDIS.getMappedPort(6379)
        );

        LettuceConnectionFactory factory = new LettuceConnectionFactory(config);
        factory.afterPropertiesSet();

        redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(factory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.afterPropertiesSet();
    }

    @AfterEach
    void flushRedis(){
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
    }
}
