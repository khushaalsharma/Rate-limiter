package com.rate_limiter.app.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "rate_limit_configs")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateLimitConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_key", unique = true, nullable = false)
    private String clientKey;

    @Column(name = "max_requests", nullable = false)
    private int maxRequests;

    @Column(name = "window_seconds", nullable = false)
    private int windowSeconds;

    @Enumerated(EnumType.STRING)
    @Column(name = "algorithm", nullable = false)
    private AlgorithmType algorithmType;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist(){
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate(){
        this.updatedAt = LocalDateTime.now();
    }

    public enum AlgorithmType{
        FIXED_WINDOW,
        SLIDING_WINDOW,
        TOKEN_BUCKET
    }
}
