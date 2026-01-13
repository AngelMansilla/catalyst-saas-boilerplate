package com.catalyst.user.domain.event;

import com.catalyst.user.domain.valueobject.Email;
import com.catalyst.user.domain.valueobject.UserId;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event published when a password has been successfully reset.
 * 
 * <p>This event is consumed by:
 * <ul>
 *   <li>notification-service - to send password changed confirmation email</li>
 * </ul>
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
public record PasswordResetCompleted(
        UUID eventId,
        Instant timestamp,
        UserId userId,
        Email email
) {
    
    public PasswordResetCompleted(UserId userId, Email email) {
        this(
            UUID.randomUUID(),
            Instant.now(),
            userId,
            email
        );
    }
    
    public String getEventType() {
        return "PasswordResetCompleted";
    }
    
    public String getVersion() {
        return "1.0";
    }
}

