package com.rate_limiter.app.repository;

import com.rate_limiter.app.models.RateLimitConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RateLimitConfigRepository extends JpaRepository<RateLimitConfig, Long> {

    Optional<RateLimitConfig> findByClientKey(String clientKey);
    boolean existsByClientKey(String clientKey);
    void deleteByClientKey(String clientKey);
}
