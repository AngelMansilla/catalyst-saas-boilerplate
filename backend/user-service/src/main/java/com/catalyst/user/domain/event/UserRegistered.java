package com.catalyst.user.domain.event;

import com.catalyst.user.domain.model.AuthProvider;
import com.catalyst.user.domain.model.UserRole;
import com.catalyst.user.domain.valueobject.Email;
import com.catalyst.user.domain.valueobject.UserId;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event published when a new user registers.
 * 
 * <p>This event is consumed by:
 * <ul>
 *   <li>notification-service - to send welcome email</li>
 *   <li>payment-service - to create Customer record</li>
 * </ul>
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
public record UserRegistered(
        UUID eventId,
        Instant timestamp,
        UserId userId,
        Email email,
        String name,
        AuthProvider provider,
        UserRole role
) {
    
    public UserRegistered(UserId userId, Email email, String name, AuthProvider provider, UserRole role) {
        this(
            UUID.randomUUID(),
            Instant.now(),
            userId,
            email,
            name,
            provider,
            role
        );
    }
    
    public String getEventType() {
        return "UserRegistered";
    }
    
    public String getVersion() {
        return "1.0";
    }
}

