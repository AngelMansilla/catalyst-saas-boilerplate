package com.catalyst.shared.domain.exception;

import java.io.Serial;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * Exception thrown when rate limit is exceeded.
 * Maps to HTTP 429 Too Many Requests.
 */
public class RateLimitExceededException extends DomainException {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    private static final int HTTP_STATUS = 429;
    
    private final long retryAfterSeconds;
    private final Instant resetTime;
    
    public RateLimitExceededException(String message, long retryAfterSeconds) {
        super("RATE_LIMIT.EXCEEDED", message, 
            Map.of("retryAfterSeconds", retryAfterSeconds));
        this.retryAfterSeconds = retryAfterSeconds;
        this.resetTime = Instant.now().plusSeconds(retryAfterSeconds);
    }
    
    public RateLimitExceededException(
            String message, 
            long retryAfterSeconds, 
            int limit, 
            int remaining) {
        super(
            "RATE_LIMIT.EXCEEDED", 
            message,
            Map.of(
                "retryAfterSeconds", retryAfterSeconds,
                "limit", limit,
                "remaining", remaining
            )
        );
        this.retryAfterSeconds = retryAfterSeconds;
        this.resetTime = Instant.now().plusSeconds(retryAfterSeconds);
    }
    
    /**
     * Creates a rate limit exception with default message.
     */
    public static RateLimitExceededException withRetryAfter(long seconds) {
        return new RateLimitExceededException(
            "Too many requests. Please try again later.",
            seconds
        );
    }
    
    /**
     * Creates a rate limit exception for API endpoint.
     */
    public static RateLimitExceededException forEndpoint(String endpoint, long retryAfter) {
        return new RateLimitExceededException(
            String.format("Rate limit exceeded for endpoint: %s", endpoint),
            retryAfter
        );
    }
    
    /**
     * Gets the number of seconds to wait before retrying.
     */
    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
    
    /**
     * Gets the time when the rate limit resets.
     */
    public Instant getResetTime() {
        return resetTime;
    }
    
    /**
     * Gets the retry-after duration.
     */
    public Duration getRetryAfterDuration() {
        return Duration.ofSeconds(retryAfterSeconds);
    }
    
    @Override
    public int getHttpStatus() {
        return HTTP_STATUS;
    }
}

