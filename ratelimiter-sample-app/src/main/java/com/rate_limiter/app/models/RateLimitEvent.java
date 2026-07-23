package com.rate_limiter.app.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "rate_limits_event")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateLimitEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", unique = true, nullable = false)
    private String eventId;

    @Column(name = "client_key", nullable = false)
    private String clientKey;

    @Column(name = "endpoint")
    private String endpoint;

    @Column(name = "allowed", nullable = false)
    private boolean allowed;

    @Column(name = "algorithm")
    private String algorithm;

    @Column(name = "remaining_requests")
    private int remainingRequests;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
}
