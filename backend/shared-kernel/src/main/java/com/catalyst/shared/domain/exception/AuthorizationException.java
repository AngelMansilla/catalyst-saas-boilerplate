package com.catalyst.shared.domain.exception;

import java.io.Serial;
import java.util.Map;
import java.util.Set;

/**
 * Exception thrown when a user doesn't have permission to perform an action.
 * Maps to HTTP 403 Forbidden.
 */
public class AuthorizationException extends DomainException {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    private static final int HTTP_STATUS = 403;
    
    public AuthorizationException(String message) {
        super("AUTH.FORBIDDEN", message);
    }
    
    public AuthorizationException(String errorCode, String message) {
        super(errorCode, message);
    }
    
    public AuthorizationException(String errorCode, String message, Map<String, Object> details) {
        super(errorCode, message, details);
    }
    
    /**
     * Creates an exception for insufficient permissions.
     */
    public static AuthorizationException insufficientPermissions() {
        return new AuthorizationException(
            "AUTH.PERMISSION.DENIED",
            "You do not have permission to perform this action"
        );
    }
    
    /**
     * Creates an exception for missing required role.
     */
    public static AuthorizationException roleRequired(String requiredRole) {
        return new AuthorizationException(
            "AUTH.ROLE.REQUIRED",
            String.format("This action requires the %s role", requiredRole),
            Map.of("requiredRole", requiredRole)
        );
    }
    
    /**
     * Creates an exception for missing any of required roles.
     */
    public static AuthorizationException anyRoleRequired(Set<String> requiredRoles) {
        return new AuthorizationException(
            "AUTH.ROLE.REQUIRED",
            String.format("This action requires one of the following roles: %s", 
                String.join(", ", requiredRoles)),
            Map.of("requiredRoles", requiredRoles)
        );
    }
    
    /**
     * Creates an exception for resource ownership violation.
     */
    public static AuthorizationException notResourceOwner() {
        return new AuthorizationException(
            "AUTH.OWNERSHIP.DENIED",
            "You can only access your own resources"
        );
    }
    
    /**
     * Creates an exception for subscription required.
     */
    public static AuthorizationException subscriptionRequired(String plan) {
        return new AuthorizationException(
            "AUTH.SUBSCRIPTION.REQUIRED",
            String.format("This feature requires a %s subscription", plan),
            Map.of("requiredPlan", plan)
        );
    }
    
    @Override
    public int getHttpStatus() {
        return HTTP_STATUS;
    }
}

