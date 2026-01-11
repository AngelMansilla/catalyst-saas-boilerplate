package com.catalyst.shared.domain.exception;

import java.io.Serial;
import java.util.Map;

/**
 * Exception thrown when a requested entity cannot be found.
 * Maps to HTTP 404 Not Found.
 */
public class EntityNotFoundException extends DomainException {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    private static final int HTTP_STATUS = 404;
    
    public EntityNotFoundException(String message) {
        super("ENTITY.NOT_FOUND", message);
    }
    
    public EntityNotFoundException(String entityType, Object identifier) {
        super(
            entityType.toUpperCase() + ".NOT_FOUND",
            String.format("%s with identifier '%s' not found", entityType, identifier),
            Map.of("entityType", entityType, "identifier", identifier.toString())
        );
    }
    
    public EntityNotFoundException(String errorCode, String message, Map<String, Object> details) {
        super(errorCode, message, details);
    }
    
    /**
     * Creates an exception for a user not found by ID.
     */
    public static EntityNotFoundException userNotFound(Object userId) {
        return new EntityNotFoundException("User", userId);
    }
    
    /**
     * Creates an exception for a user not found by email.
     */
    public static EntityNotFoundException userNotFoundByEmail(String email) {
        return new EntityNotFoundException(
            "USER.NOT_FOUND_BY_EMAIL",
            "User with email not found",
            Map.of("email", email)
        );
    }
    
    /**
     * Creates an exception for a subscription not found.
     */
    public static EntityNotFoundException subscriptionNotFound(Object subscriptionId) {
        return new EntityNotFoundException("Subscription", subscriptionId);
    }
    
    /**
     * Creates an exception for a payment not found.
     */
    public static EntityNotFoundException paymentNotFound(Object paymentId) {
        return new EntityNotFoundException("Payment", paymentId);
    }
    
    @Override
    public int getHttpStatus() {
        return HTTP_STATUS;
    }
}

