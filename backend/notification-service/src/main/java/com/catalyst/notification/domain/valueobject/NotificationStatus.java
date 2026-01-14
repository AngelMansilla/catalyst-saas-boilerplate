package com.catalyst.notification.domain.valueobject;

/**
 * Enumeration of notification statuses.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
public enum NotificationStatus {
    PENDING("Pending delivery"),
    SENT("Successfully sent"),
    FAILED("Delivery failed"),
    RETRYING("Retrying delivery");
    
    private final String description;
    
    NotificationStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}

