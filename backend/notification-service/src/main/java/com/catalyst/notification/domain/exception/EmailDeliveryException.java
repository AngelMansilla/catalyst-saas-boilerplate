package com.catalyst.notification.domain.exception;

/**
 * Exception thrown when email delivery fails.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
public class EmailDeliveryException extends NotificationException {
    
    public EmailDeliveryException(String message) {
        super(message);
    }
    
    public EmailDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}

