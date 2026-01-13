package com.catalyst.user.domain.exception;

import com.catalyst.shared.domain.exception.AuthenticationException;

/**
 * Exception thrown when a password reset token is invalid.
 * 
 * <p>This can occur when:
 * <ul>
 *   <li>Token does not exist</li>
 *   <li>Token has expired</li>
 *   <li>Token has already been used</li>
 * </ul>
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
public class InvalidResetTokenException extends AuthenticationException {
    
    public InvalidResetTokenException(String message) {
        super(message);
    }
    
    public static InvalidResetTokenException notFound() {
        return new InvalidResetTokenException("Invalid or expired password reset token");
    }
    
    public static InvalidResetTokenException expired() {
        return new InvalidResetTokenException("Password reset token has expired");
    }
    
    public static InvalidResetTokenException alreadyUsed() {
        return new InvalidResetTokenException("Password reset token has already been used");
    }
}

