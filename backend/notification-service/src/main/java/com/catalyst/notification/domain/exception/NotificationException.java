package com.catalyst.notification.domain.exception;

/**
 * Base exception for notification domain errors.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
public class NotificationException extends RuntimeException {
    
    public NotificationException(String message) {
        super(message);
    }
    
    public NotificationException(String message, Throwable cause) {
        super(message, cause);
    }
}

