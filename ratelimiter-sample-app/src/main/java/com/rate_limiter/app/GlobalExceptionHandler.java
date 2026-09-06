package com.rate_limiter.app;

import io.github.khushaalsharma.ratelimiter.autoconfigure.aop.RateLimitExceededException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<String> handleRateLimitExceeded(RateLimitExceededException e){
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body("Rate Limit exceeded, remaining" + e.decision().remainingRequests());
    }
}
