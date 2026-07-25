package io.github.khushaalsharma.ratelimiter.autoconfigure.analytics;

import java.time.Instant;

public class RateLimitEventDto {
    private String eventId;
    private String clientKey;
    private String endpoint;
    private boolean allowed;
    private String algorithm;
    private long remainingRequests;
    private Instant timestamp;

    public RateLimitEventDto(String eventId, String clientKey, String endpoint, boolean allowed, String algorithm, long remainingRequests, Instant timestamp) {
        this.eventId = eventId;
        this.clientKey = clientKey;
        this.endpoint = endpoint;
        this.allowed = allowed;
        this.algorithm = algorithm;
        this.remainingRequests = remainingRequests;
        this.timestamp = timestamp;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getClientKey() {
        return clientKey;
    }

    public void setClientKey(String clientKey) {
        this.clientKey = clientKey;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public void setAllowed(boolean allowed) {
        this.allowed = allowed;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public long getRemainingRequests() {
        return remainingRequests;
    }

    public void setRemainingRequests(long remainingRequests) {
        this.remainingRequests = remainingRequests;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
