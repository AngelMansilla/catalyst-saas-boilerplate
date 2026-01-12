package com.catalyst.payment.domain.exception;

import java.util.Map;
import java.util.UUID;

/**
 * Exception thrown when a subscription is not found.
 */
public class SubscriptionNotFoundException extends PaymentException {
    
    public SubscriptionNotFoundException(UUID subscriptionId) {
        super("SUBSCRIPTION.NOT_FOUND", "Subscription not found: " + subscriptionId);
        addDetail("subscriptionId", subscriptionId);
    }

    public SubscriptionNotFoundException(String message) {
        super("SUBSCRIPTION.NOT_FOUND", message);
    }

    @Override
    public int getHttpStatus() {
        return 404; // Not Found
    }
}

