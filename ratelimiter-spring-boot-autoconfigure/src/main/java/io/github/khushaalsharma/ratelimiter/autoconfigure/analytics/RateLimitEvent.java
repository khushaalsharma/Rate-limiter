package io.github.khushaalsharma.ratelimiter.autoconfigure.analytics;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "rate_limit_events")
public class RateLimitEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true)
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
    private long remainingRequests;

    @Column(name = "timestamp", nullable = false)
    private long timestamp;

    protected RateLimitEvent(){}

    public RateLimitEvent(String eventId, String clientKey, String endpoint, boolean allowed, String algorithm, long remainingRequests, long timestamp) {
        this.eventId = eventId;
        this.clientKey = clientKey;
        this.endpoint = endpoint;
        this.allowed = allowed;
        this.algorithm = algorithm;
        this.remainingRequests = remainingRequests;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public String getEventId() {
        return eventId;
    }

    public String getClientKey() {
        return clientKey;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public long getRemainingRequests() {
        return remainingRequests;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
