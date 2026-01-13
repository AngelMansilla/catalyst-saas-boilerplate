package com.catalyst.user.domain.exception;

import com.catalyst.shared.domain.exception.EntityNotFoundException;
import com.catalyst.user.domain.valueobject.Email;
import com.catalyst.user.domain.valueobject.UserId;

import java.util.Map;

/**
 * Exception thrown when a user cannot be found.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
public class UserNotFoundException extends EntityNotFoundException {
    
    public UserNotFoundException(UserId userId) {
        super("User", userId.toString());
    }
    
    public UserNotFoundException(Email email) {
        super(
            "USER.NOT_FOUND_BY_EMAIL",
            String.format("User with email '%s' not found", email.getValue()),
            Map.of("email", email.getValue())
        );
    }
    
    public UserNotFoundException(String message) {
        super(message);
    }
}

