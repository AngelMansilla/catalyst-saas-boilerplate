package com.catalyst.payment.application.ports.input;

import com.catalyst.payment.application.dto.SubscriptionDto;

import java.util.List;
import java.util.UUID;

/**
 * Input port for retrieving subscription information.
 */
public interface GetSubscriptionUseCase {
    
    /**
     * Gets a subscription by ID.
     *
     * @param subscriptionId the subscription ID
     * @return the subscription DTO
     */
    SubscriptionDto getById(UUID subscriptionId);

    /**
     * Gets the active subscription for a user.
     *
     * @param userId the user ID
     * @return the active subscription DTO, or null if none
     */
    SubscriptionDto getActiveByUserId(UUID userId);

    /**
     * Gets all subscriptions for a user.
     *
     * @param userId the user ID
     * @return list of subscription DTOs
     */
    List<SubscriptionDto> getAllByUserId(UUID userId);

    /**
     * Checks if a user has an active subscription.
     *
     * @param userId the user ID
     * @return true if the user has an active subscription
     */
    boolean hasActiveSubscription(UUID userId);
}

