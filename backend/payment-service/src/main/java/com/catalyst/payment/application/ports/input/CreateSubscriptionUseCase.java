package com.catalyst.payment.application.ports.input;

import com.catalyst.payment.application.dto.CreateSubscriptionRequest;
import com.catalyst.payment.application.dto.CreateSubscriptionResponse;

/**
 * Input port for creating a subscription (checkout session).
 */
public interface CreateSubscriptionUseCase {
    
    /**
     * Creates a checkout session for a new subscription.
     *
     * @param request the subscription request
     * @return the checkout response with session URL
     */
    CreateSubscriptionResponse execute(CreateSubscriptionRequest request);
}

