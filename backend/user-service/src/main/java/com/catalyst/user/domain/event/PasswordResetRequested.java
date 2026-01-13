package com.catalyst.user.domain.event;

import com.catalyst.user.domain.valueobject.Email;
import com.catalyst.user.domain.valueobject.UserId;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event published when a password reset is requested.
 * 
 * <p>This event is consumed by:
 * <ul>
 *   <li>notification-service - to send password reset email with link</li>
 * </ul>
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
public record PasswordResetRequested(
        UUID eventId,
        Instant timestamp,
        UserId userId,
        Email email,
        String token,
        LocalDateTime expiresAt
) {
    
    public PasswordResetRequested(UserId userId, Email email, String token, LocalDateTime expiresAt) {
        this(
            UUID.randomUUID(),
            Instant.now(),
            userId,
            email,
            token,
            expiresAt
        );
    }
    
    public String getEventType() {
        return "PasswordResetRequested";
    }
    
    public String getVersion() {
        return "1.0";
    }
}

