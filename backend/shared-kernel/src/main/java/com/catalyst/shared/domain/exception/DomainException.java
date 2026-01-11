package com.catalyst.shared.domain.exception;

import java.io.Serial;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Base exception for all domain-level exceptions.
 * Framework-agnostic - no Spring dependencies.
 * 
 * <p>Error code format: CATEGORY.SUBCATEGORY.SPECIFIC
 * <p>Example: AUTH.TOKEN.EXPIRED, USER.NOT_FOUND, PAYMENT.CARD.DECLINED
 */
public abstract class DomainException extends RuntimeException {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    private final String errorCode;
    private final Instant timestamp;
    private final Map<String, Object> details;
    
    protected DomainException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.timestamp = Instant.now();
        this.details = new HashMap<>();
    }
    
    protected DomainException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.timestamp = Instant.now();
        this.details = new HashMap<>();
    }
    
    protected DomainException(String errorCode, String message, Map<String, Object> details) {
        super(message);
        this.errorCode = errorCode;
        this.timestamp = Instant.now();
        this.details = details != null ? new HashMap<>(details) : new HashMap<>();
    }
    
    protected DomainException(String errorCode, String message, Map<String, Object> details, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.timestamp = Instant.now();
        this.details = details != null ? new HashMap<>(details) : new HashMap<>();
    }
    
    /**
     * Gets the error code in format CATEGORY.SUBCATEGORY.SPECIFIC.
     */
    public String getErrorCode() {
        return errorCode;
    }
    
    /**
     * Gets the timestamp when the exception was created.
     */
    public Instant getTimestamp() {
        return timestamp;
    }
    
    /**
     * Gets additional details about the error.
     */
    public Map<String, Object> getDetails() {
        return Collections.unmodifiableMap(details);
    }
    
    /**
     * Adds a detail to this exception.
     */
    public DomainException addDetail(String key, Object value) {
        this.details.put(key, value);
        return this;
    }
    
    /**
     * Gets the error category (first part of error code).
     */
    public String getCategory() {
        int dotIndex = errorCode.indexOf('.');
        return dotIndex > 0 ? errorCode.substring(0, dotIndex) : errorCode;
    }
    
    /**
     * Returns the HTTP status code that should be returned for this exception.
     * Subclasses should override to provide appropriate status codes.
     */
    public abstract int getHttpStatus();
}

