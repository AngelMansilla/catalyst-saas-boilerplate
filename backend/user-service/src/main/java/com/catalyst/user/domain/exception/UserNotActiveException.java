package com.catalyst.user.domain.exception;

import com.catalyst.shared.domain.exception.AuthenticationException;

/**
 * Exception thrown when attempting to authenticate with an inactive account.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
public class UserNotActiveException extends AuthenticationException {
    
    public UserNotActiveException() {
        super("User account is not active");
    }
    
    public UserNotActiveException(String message) {
        super(message);
    }
}

