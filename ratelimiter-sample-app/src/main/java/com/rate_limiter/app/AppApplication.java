package com.rate_limiter.app;

import io.github.khushaalsharma.ratelimiter.autoconfigure.annotation.EnableRateLimiting;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.data.redis.autoconfigure.DataRedisRepositoriesAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableRateLimiting
@EnableJpaRepositories
@SpringBootApplication
public class AppApplication {

	public static void main(String[] args) {
		SpringApplication.run(AppApplication.class, args);
	}

}
