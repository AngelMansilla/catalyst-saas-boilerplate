package com.catalyst.user.domain.exception;

import com.catalyst.shared.domain.exception.EntityAlreadyExistsException;
import com.catalyst.user.domain.valueobject.Email;

/**
 * Exception thrown when attempting to register with an email that already exists.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
public class EmailAlreadyExistsException extends EntityAlreadyExistsException {
    
    public EmailAlreadyExistsException(Email email) {
        super("User", "email", email.getValue());
    }
    
    public EmailAlreadyExistsException(String email) {
        super("User", "email", email);
    }
}

