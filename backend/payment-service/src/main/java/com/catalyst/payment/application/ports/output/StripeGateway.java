package com.catalyst.payment.application.ports.output;

import com.catalyst.payment.domain.model.BillingCycle;
import com.catalyst.payment.domain.model.SubscriptionTier;

import java.util.UUID;

/**
 * Output port for Stripe payment gateway operations.
 */
public interface StripeGateway {
    
    /**
     * Creates a Stripe customer.
     *
     * @param email the customer email
     * @param name the customer name
     * @return the Stripe customer ID
     */
    String createCustomer(String email, String name);

    /**
     * Creates a checkout session for subscription.
     *
     * @param stripeCustomerId the Stripe customer ID
     * @param tier the subscription tier
     * @param billingCycle the billing cycle
     * @param successUrl the success redirect URL
     * @param cancelUrl the cancel redirect URL
     * @return the checkout session URL
     */
    String createCheckoutSession(String stripeCustomerId, SubscriptionTier tier, 
                                  BillingCycle billingCycle, String successUrl, String cancelUrl);

    /**
     * Creates a customer portal session.
     *
     * @param stripeCustomerId the Stripe customer ID
     * @param returnUrl the return URL
     * @return the portal session URL
     */
    String createCustomerPortalSession(String stripeCustomerId, String returnUrl);

    /**
     * Cancels a subscription in Stripe.
     *
     * @param stripeSubscriptionId the Stripe subscription ID
     * @param immediate whether to cancel immediately or at period end
     */
    void cancelSubscription(String stripeSubscriptionId, boolean immediate);

    /**
     * Retrieves a subscription from Stripe.
     *
     * @param stripeSubscriptionId the Stripe subscription ID
     * @return the subscription details
     */
    StripeSubscriptionDetails getSubscription(String stripeSubscriptionId);

    /**
     * Verifies a webhook signature.
     *
     * @param payload the raw payload
     * @param signature the Stripe signature header
     * @return true if the signature is valid
     */
    boolean verifyWebhookSignature(String payload, String signature);

    /**
     * Parses a webhook event.
     *
     * @param payload the raw payload
     * @param signature the Stripe signature header
     * @return the parsed event
     */
    StripeWebhookEvent parseWebhookEvent(String payload, String signature);

    /**
     * DTO for Stripe subscription details.
     */
    record StripeSubscriptionDetails(
        String subscriptionId,
        String customerId,
        String status,
        String priceId,
        Long currentPeriodStart,
        Long currentPeriodEnd,
        Long trialEnd,
        boolean cancelAtPeriodEnd
    ) {}

    /**
     * DTO for Stripe webhook events.
     */
    record StripeWebhookEvent(
        String id,
        String type,
        Object data
    ) {}
}

