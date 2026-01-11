package com.catalyst.shared.domain.exception;

import java.io.Serial;
import java.util.Map;

/**
 * Exception thrown when trying to create an entity that already exists.
 * Maps to HTTP 409 Conflict.
 */
public class EntityAlreadyExistsException extends DomainException {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    private static final int HTTP_STATUS = 409;
    
    public EntityAlreadyExistsException(String message) {
        super("ENTITY.ALREADY_EXISTS", message);
    }
    
    public EntityAlreadyExistsException(String entityType, String field, Object value) {
        super(
            entityType.toUpperCase() + ".ALREADY_EXISTS",
            String.format("%s with %s '%s' already exists", entityType, field, value),
            Map.of("entityType", entityType, "field", field, "value", value.toString())
        );
    }
    
    public EntityAlreadyExistsException(String errorCode, String message, Map<String, Object> details) {
        super(errorCode, message, details);
    }
    
    /**
     * Creates an exception for email already registered.
     */
    public static EntityAlreadyExistsException emailAlreadyExists(String email) {
        return new EntityAlreadyExistsException("User", "email", email);
    }
    
    /**
     * Creates an exception for username already taken.
     */
    public static EntityAlreadyExistsException usernameAlreadyTaken(String username) {
        return new EntityAlreadyExistsException("User", "username", username);
    }
    
    @Override
    public int getHttpStatus() {
        return HTTP_STATUS;
    }
}

