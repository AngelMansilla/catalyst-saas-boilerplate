package com.catalyst.shared.domain.exception;

import java.io.Serial;
import java.util.Map;

/**
 * Exception thrown when an external service fails.
 * Maps to HTTP 502 Bad Gateway or 503 Service Unavailable.
 */
public class ExternalServiceException extends DomainException {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    private final String serviceName;
    private final boolean retryable;
    
    public ExternalServiceException(String serviceName, String message) {
        super(
            "EXTERNAL." + serviceName.toUpperCase() + ".FAILED",
            message,
            Map.of("serviceName", serviceName)
        );
        this.serviceName = serviceName;
        this.retryable = true;
    }
    
    public ExternalServiceException(String serviceName, String message, boolean retryable) {
        super(
            "EXTERNAL." + serviceName.toUpperCase() + ".FAILED",
            message,
            Map.of("serviceName", serviceName, "retryable", retryable)
        );
        this.serviceName = serviceName;
        this.retryable = retryable;
    }
    
    public ExternalServiceException(String serviceName, String message, Throwable cause) {
        super(
            "EXTERNAL." + serviceName.toUpperCase() + ".FAILED",
            message,
            Map.of("serviceName", serviceName),
            cause
        );
        this.serviceName = serviceName;
        this.retryable = true;
    }
    
    /**
     * Creates an exception for Stripe service failure.
     */
    public static ExternalServiceException stripeError(String message) {
        return new ExternalServiceException("STRIPE", message);
    }
    
    /**
     * Creates an exception for Stripe payment failure.
     */
    public static ExternalServiceException stripePaymentFailed(String message, String declineCode) {
        var exception = new ExternalServiceException("STRIPE", message, false);
        exception.addDetail("declineCode", declineCode);
        return exception;
    }
    
    /**
     * Creates an exception for email service failure.
     */
    public static ExternalServiceException emailServiceError(String message) {
        return new ExternalServiceException("EMAIL", message);
    }
    
    /**
     * Creates an exception for S3 service failure.
     */
    public static ExternalServiceException s3Error(String message) {
        return new ExternalServiceException("S3", message);
    }
    
    /**
     * Creates an exception for Kafka messaging failure.
     */
    public static ExternalServiceException kafkaError(String message) {
        return new ExternalServiceException("KAFKA", message);
    }
    
    /**
     * Creates an exception for Redis failure.
     */
    public static ExternalServiceException redisError(String message) {
        return new ExternalServiceException("REDIS", message);
    }
    
    /**
     * Gets the name of the failed service.
     */
    public String getServiceName() {
        return serviceName;
    }
    
    /**
     * Whether the operation can be retried.
     */
    public boolean isRetryable() {
        return retryable;
    }
    
    @Override
    public int getHttpStatus() {
        return retryable ? 503 : 502;
    }
}

