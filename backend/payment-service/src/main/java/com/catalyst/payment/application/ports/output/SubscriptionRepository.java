package com.catalyst.payment.application.ports.output;

import com.catalyst.payment.domain.model.Subscription;
import com.catalyst.payment.domain.model.SubscriptionStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Output port for subscription persistence operations.
 */
public interface SubscriptionRepository {
    
    /**
     * Saves a subscription.
     *
     * @param subscription the subscription to save
     * @return the saved subscription
     */
    Subscription save(Subscription subscription);

    /**
     * Finds a subscription by ID.
     *
     * @param id the subscription ID
     * @return an optional containing the subscription if found
     */
    Optional<Subscription> findById(UUID id);

    /**
     * Finds a subscription by Stripe subscription ID.
     *
     * @param stripeSubscriptionId the Stripe subscription ID
     * @return an optional containing the subscription if found
     */
    Optional<Subscription> findByStripeSubscriptionId(String stripeSubscriptionId);

    /**
     * Finds all subscriptions for a customer.
     *
     * @param customerId the customer ID
     * @return list of subscriptions
     */
    List<Subscription> findByCustomerId(UUID customerId);

    /**
     * Finds the active subscription for a customer.
     *
     * @param customerId the customer ID
     * @return an optional containing the active subscription if found
     */
    Optional<Subscription> findActiveByCustomerId(UUID customerId);

    /**
     * Finds subscriptions by status.
     *
     * @param status the subscription status
     * @return list of subscriptions with the given status
     */
    List<Subscription> findByStatus(SubscriptionStatus status);

    /**
     * Finds subscriptions with expired trials.
     *
     * @return list of subscriptions with expired trials
     */
    List<Subscription> findExpiredTrials();

    /**
     * Checks if a customer has an active subscription.
     *
     * @param customerId the customer ID
     * @return true if the customer has an active subscription
     */
    boolean existsActiveByCustomerId(UUID customerId);

    /**
     * Deletes a subscription.
     *
     * @param id the subscription ID
     */
    void deleteById(UUID id);
}

