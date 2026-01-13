package com.catalyst.user.domain.exception;

import com.catalyst.shared.domain.exception.AuthenticationException;

/**
 * Exception thrown when password validation fails.
 * 
 * <p>This can occur when:
 * <ul>
 *   <li>Password does not meet requirements</li>
 *   <li>Password does not match stored hash</li>
 *   <li>Attempting password operations on social login users</li>
 * </ul>
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
public class InvalidPasswordException extends AuthenticationException {
    
    public InvalidPasswordException(String message) {
        super(message);
    }
    
    public static InvalidPasswordException invalidCredentials() {
        return new InvalidPasswordException("Invalid email or password");
    }
    
    public static InvalidPasswordException weakPassword() {
        return new InvalidPasswordException(
            "Password must be at least 8 characters and contain uppercase, lowercase, number, and special character"
        );
    }
    
    public static InvalidPasswordException mismatch() {
        return new InvalidPasswordException("Passwords do not match");
    }
}

