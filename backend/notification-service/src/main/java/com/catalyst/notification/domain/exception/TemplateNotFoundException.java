package com.catalyst.notification.domain.exception;

/**
 * Exception thrown when an email template cannot be found.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
public class TemplateNotFoundException extends NotificationException {
    
    public TemplateNotFoundException(String templateName) {
        super("Email template not found: " + templateName);
    }
}

