package com.catalyst.payment.domain.exception;

/**
 * Exception thrown when an invalid subscription state transition is attempted.
 */
public class InvalidSubscriptionStateException extends PaymentException {
    
    public InvalidSubscriptionStateException(String message) {
        super("SUBSCRIPTION.INVALID_STATE", message);
    }

    public InvalidSubscriptionStateException(String from, String to) {
        super("SUBSCRIPTION.INVALID_TRANSITION", 
            String.format("Cannot transition subscription from %s to %s", from, to));
        addDetail("fromStatus", from);
        addDetail("toStatus", to);
    }

    @Override
    public int getHttpStatus() {
        return 409; // Conflict
    }
}

