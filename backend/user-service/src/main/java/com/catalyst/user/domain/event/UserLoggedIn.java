package com.catalyst.user.domain.event;

import com.catalyst.user.domain.model.AuthProvider;
import com.catalyst.user.domain.valueobject.Email;
import com.catalyst.user.domain.valueobject.UserId;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event published when a user successfully logs in.
 * 
 * <p>This event is useful for:
 * <ul>
 *   <li>Security auditing</li>
 *   <li>Analytics tracking</li>
 *   <li>Suspicious activity detection</li>
 * </ul>
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
public record UserLoggedIn(
        UUID eventId,
        Instant timestamp,
        UserId userId,
        Email email,
        AuthProvider provider,
        String ipAddress
) {
    
    public UserLoggedIn(UserId userId, Email email, AuthProvider provider, String ipAddress) {
        this(
            UUID.randomUUID(),
            Instant.now(),
            userId,
            email,
            provider,
            ipAddress
        );
    }
    
    public String getEventType() {
        return "UserLoggedIn";
    }
    
    public String getVersion() {
        return "1.0";
    }
}

