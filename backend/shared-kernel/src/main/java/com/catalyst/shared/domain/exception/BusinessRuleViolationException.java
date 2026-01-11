package com.catalyst.shared.domain.exception;

import java.io.Serial;
import java.util.Map;

/**
 * Exception thrown when a business rule is violated.
 * Maps to HTTP 422 Unprocessable Entity.
 */
public class BusinessRuleViolationException extends DomainException {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    private static final int HTTP_STATUS = 422;
    
    public BusinessRuleViolationException(String message) {
        super("BUSINESS.RULE_VIOLATION", message);
    }
    
    public BusinessRuleViolationException(String errorCode, String message) {
        super(errorCode, message);
    }
    
    public BusinessRuleViolationException(String errorCode, String message, Map<String, Object> details) {
        super(errorCode, message, details);
    }
    
    /**
     * Creates an exception for subscription already active.
     */
    public static BusinessRuleViolationException subscriptionAlreadyActive() {
        return new BusinessRuleViolationException(
            "SUBSCRIPTION.ALREADY_ACTIVE",
            "You already have an active subscription"
        );
    }
    
    /**
     * Creates an exception for subscription not active.
     */
    public static BusinessRuleViolationException subscriptionNotActive() {
        return new BusinessRuleViolationException(
            "SUBSCRIPTION.NOT_ACTIVE",
            "Your subscription is not active"
        );
    }
    
    /**
     * Creates an exception for payment already processed.
     */
    public static BusinessRuleViolationException paymentAlreadyProcessed() {
        return new BusinessRuleViolationException(
            "PAYMENT.ALREADY_PROCESSED",
            "This payment has already been processed"
        );
    }
    
    /**
     * Creates an exception for quota exceeded.
     */
    public static BusinessRuleViolationException quotaExceeded(String resource, int limit) {
        return new BusinessRuleViolationException(
            "QUOTA.EXCEEDED",
            String.format("You have exceeded your %s quota (limit: %d)", resource, limit),
            Map.of("resource", resource, "limit", limit)
        );
    }
    
    /**
     * Creates an exception for invalid state transition.
     */
    public static BusinessRuleViolationException invalidStateTransition(
            String entity, String currentState, String targetState) {
        return new BusinessRuleViolationException(
            entity.toUpperCase() + ".INVALID_STATE_TRANSITION",
            String.format("Cannot transition %s from %s to %s", entity, currentState, targetState),
            Map.of("entity", entity, "currentState", currentState, "targetState", targetState)
        );
    }
    
    /**
     * Creates an exception for operation not allowed.
     */
    public static BusinessRuleViolationException operationNotAllowed(String operation, String reason) {
        return new BusinessRuleViolationException(
            "OPERATION.NOT_ALLOWED",
            String.format("Operation '%s' is not allowed: %s", operation, reason),
            Map.of("operation", operation, "reason", reason)
        );
    }
    
    @Override
    public int getHttpStatus() {
        return HTTP_STATUS;
    }
}

