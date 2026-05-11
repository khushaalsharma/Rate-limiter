package com.rate_limiter.app.config;

import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class ApiKeyService {
    private final Set<String> validKeys = Set.of(
            "secret-key-abc123",
            "another-secret-xyz890"
    );

    public boolean isValid(String keyValue) {
        return validKeys.contains(keyValue);
    }
}
