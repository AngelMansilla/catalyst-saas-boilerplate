package com.catalyst.user.application.ports.output;

import com.catalyst.user.domain.event.PasswordResetCompleted;
import com.catalyst.user.domain.event.PasswordResetRequested;
import com.catalyst.user.domain.event.UserLoggedIn;
import com.catalyst.user.domain.event.UserRegistered;

/**
 * Output port for publishing domain events.
 * Events are published to Kafka for consumption by other services.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
public interface EventPublisher {
    
    /**
     * Publishes a user registered event.
     * Consumed by notification-service and payment-service.
     * 
     * @param event the event to publish
     */
    void publish(UserRegistered event);
    
    /**
     * Publishes a user logged in event.
     * Consumed by audit/analytics services.
     * 
     * @param event the event to publish
     */
    void publish(UserLoggedIn event);
    
    /**
     * Publishes a password reset requested event.
     * Consumed by notification-service to send email.
     * 
     * @param event the event to publish
     */
    void publish(PasswordResetRequested event);
    
    /**
     * Publishes a password reset completed event.
     * Consumed by notification-service to send confirmation.
     * 
     * @param event the event to publish
     */
    void publish(PasswordResetCompleted event);
}

