package com.catalyst.shared.domain.exception;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Exception thrown when input validation fails.
 * Maps to HTTP 400 Bad Request.
 */
public class ValidationException extends DomainException {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    private static final int HTTP_STATUS = 400;
    
    private final List<FieldError> fieldErrors;
    
    public ValidationException(String message) {
        super("VALIDATION.FAILED", message);
        this.fieldErrors = new ArrayList<>();
    }
    
    public ValidationException(String message, List<FieldError> fieldErrors) {
        super("VALIDATION.FAILED", message, Map.of("fieldErrors", fieldErrors));
        this.fieldErrors = new ArrayList<>(fieldErrors);
    }
    
    public ValidationException(String errorCode, String message) {
        super(errorCode, message);
        this.fieldErrors = new ArrayList<>();
    }
    
    public ValidationException(String field, String message, String code) {
        super(
            "VALIDATION.FIELD." + code.toUpperCase(),
            message,
            Map.of("field", field)
        );
        this.fieldErrors = List.of(new FieldError(field, message, code));
    }
    
    /**
     * Gets the list of field-level validation errors.
     */
    public List<FieldError> getFieldErrors() {
        return List.copyOf(fieldErrors);
    }
    
    /**
     * Creates a validation exception for a required field.
     */
    public static ValidationException required(String field) {
        return new ValidationException(
            field,
            String.format("%s is required", field),
            "REQUIRED"
        );
    }
    
    /**
     * Creates a validation exception for an invalid format.
     */
    public static ValidationException invalidFormat(String field, String expectedFormat) {
        return new ValidationException(
            field,
            String.format("%s has invalid format. Expected: %s", field, expectedFormat),
            "INVALID_FORMAT"
        );
    }
    
    /**
     * Creates a validation exception for a value too short.
     */
    public static ValidationException tooShort(String field, int minLength) {
        return new ValidationException(
            field,
            String.format("%s must be at least %d characters", field, minLength),
            "TOO_SHORT"
        );
    }
    
    /**
     * Creates a validation exception for a value too long.
     */
    public static ValidationException tooLong(String field, int maxLength) {
        return new ValidationException(
            field,
            String.format("%s must be at most %d characters", field, maxLength),
            "TOO_LONG"
        );
    }
    
    /**
     * Creates a validation exception for an invalid email.
     */
    public static ValidationException invalidEmail(String email) {
        return new ValidationException(
            "email",
            "Invalid email format",
            "INVALID_EMAIL"
        );
    }
    
    @Override
    public int getHttpStatus() {
        return HTTP_STATUS;
    }
    
    /**
     * Represents a field-level validation error.
     */
    public record FieldError(
        String field,
        String message,
        String code
    ) {}
}

