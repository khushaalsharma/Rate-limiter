package com.rate_limiter.app.repository;

import com.rate_limiter.app.models.RateLimitEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RateLimitEventRepository extends JpaRepository<RateLimitEvent, Long> {
    List<RateLimitEvent> findByClientKeyAndTimestampBetween(String key, LocalDateTime from, LocalDateTime to);

    @Query("SELECT COUNT(e) FROM RateLimitEvent e WHERE e.clientKey = :key AND e.allowed = true AND e.timestamp BETWEEN :from AND :to")
    long countAllowedByClientKeyAndTimestampBetween(
            @Param("key") String clientKey,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    @Query("SELECT COUNT(e) FROM RateLimitEvent e WHERE e.clientKey = :key AND e.allowed = false AND e.timestamp BETWEEN :from AND :to")
    long countRejectedByClientKeyAndTimestampBetween(
            @Param("key") String clientKey,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);
}
