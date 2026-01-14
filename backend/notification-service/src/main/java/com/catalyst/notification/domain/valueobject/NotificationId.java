package com.catalyst.notification.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

/**
 * Value object representing a unique notification identifier.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
public record NotificationId(UUID value) {
    
    public NotificationId {
        Objects.requireNonNull(value, "NotificationId value cannot be null");
    }
    
    public static NotificationId generate() {
        return new NotificationId(UUID.randomUUID());
    }
    
    public static NotificationId fromString(String id) {
        return new NotificationId(UUID.fromString(id));
    }
    
    @Override
    public String toString() {
        return value.toString();
    }
}

