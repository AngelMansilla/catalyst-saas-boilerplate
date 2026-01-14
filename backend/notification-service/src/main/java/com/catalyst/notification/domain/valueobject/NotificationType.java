package com.catalyst.notification.domain.valueobject;

/**
 * Enumeration of notification types supported by the system.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
public enum NotificationType {
    WELCOME("welcome"),
    PASSWORD_RESET("password-reset"),
    PASSWORD_RESET_CONFIRMATION("password-reset-confirmation"),
    SUBSCRIPTION_CREATED("subscription-created"),
    PAYMENT_RECEIPT("payment-receipt"),
    PAYMENT_FAILED("payment-failed"),
    SUBSCRIPTION_CANCELED("subscription-canceled"),
    TRIAL_EXPIRED("trial-expired");
    
    private final String code;
    
    NotificationType(String code) {
        this.code = code;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getTemplateName() {
        return code.replace("-", "");
    }
    
    public static NotificationType fromCode(String code) {
        for (NotificationType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown notification type: " + code);
    }
}

