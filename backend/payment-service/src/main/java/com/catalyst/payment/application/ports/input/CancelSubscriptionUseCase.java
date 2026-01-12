package com.catalyst.payment.application.ports.input;

import com.catalyst.payment.application.dto.CancelSubscriptionRequest;
import com.catalyst.payment.application.dto.SubscriptionDto;

/**
 * Input port for canceling a subscription.
 */
public interface CancelSubscriptionUseCase {
    
    /**
     * Cancels a subscription.
     *
     * @param request the cancellation request
     * @return the updated subscription DTO
     */
    SubscriptionDto execute(CancelSubscriptionRequest request);
}

