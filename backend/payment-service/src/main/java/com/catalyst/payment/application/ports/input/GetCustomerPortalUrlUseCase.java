package com.catalyst.payment.application.ports.input;

import java.util.UUID;

/**
 * Input port for getting the Stripe Customer Portal URL.
 */
public interface GetCustomerPortalUrlUseCase {
    
    /**
     * Gets the customer portal URL for a user.
     *
     * @param userId the user ID
     * @param returnUrl the URL to return to after portal session
     * @return the customer portal URL
     */
    String execute(UUID userId, String returnUrl);
}

