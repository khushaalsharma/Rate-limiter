package io.github.khushaalsharma.ratelimiter.autoconfigure.analytics;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RateLimitEventRepository extends JpaRepository<RateLimitEvent, Long> {
    Optional<RateLimitEvent> findByEventId(String eventId);
}
