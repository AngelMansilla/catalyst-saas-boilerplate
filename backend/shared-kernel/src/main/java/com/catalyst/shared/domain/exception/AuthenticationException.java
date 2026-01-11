package com.catalyst.shared.domain.exception;

import java.io.Serial;
import java.util.Map;

/**
 * Exception thrown when authentication fails.
 * Maps to HTTP 401 Unauthorized.
 */
public class AuthenticationException extends DomainException {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    private static final int HTTP_STATUS = 401;
    
    public AuthenticationException(String message) {
        super("AUTH.FAILED", message);
    }
    
    public AuthenticationException(String errorCode, String message) {
        super(errorCode, message);
    }
    
    public AuthenticationException(String errorCode, String message, Map<String, Object> details) {
        super(errorCode, message, details);
    }
    
    /**
     * Creates an exception for invalid credentials.
     */
    public static AuthenticationException invalidCredentials() {
        return new AuthenticationException(
            "AUTH.LOGIN.INVALID_CREDENTIALS",
            "Invalid email or password"
        );
    }
    
    /**
     * Creates an exception for an expired token.
     */
    public static AuthenticationException tokenExpired() {
        return new AuthenticationException(
            "AUTH.TOKEN.EXPIRED",
            "Your session has expired. Please log in again."
        );
    }
    
    /**
     * Creates an exception for an invalid token.
     */
    public static AuthenticationException invalidToken() {
        return new AuthenticationException(
            "AUTH.TOKEN.INVALID",
            "The provided token is invalid"
        );
    }
    
    /**
     * Creates an exception for a revoked token.
     */
    public static AuthenticationException tokenRevoked() {
        return new AuthenticationException(
            "AUTH.TOKEN.REVOKED",
            "This session has been revoked"
        );
    }
    
    /**
     * Creates an exception for missing token.
     */
    public static AuthenticationException missingToken() {
        return new AuthenticationException(
            "AUTH.TOKEN.MISSING",
            "Authentication token is required"
        );
    }
    
    /**
     * Creates an exception for account not verified.
     */
    public static AuthenticationException accountNotVerified() {
        return new AuthenticationException(
            "AUTH.ACCOUNT.NOT_VERIFIED",
            "Please verify your email address before logging in"
        );
    }
    
    /**
     * Creates an exception for account locked.
     */
    public static AuthenticationException accountLocked() {
        return new AuthenticationException(
            "AUTH.ACCOUNT.LOCKED",
            "Your account has been locked due to multiple failed login attempts"
        );
    }
    
    @Override
    public int getHttpStatus() {
        return HTTP_STATUS;
    }
}

