package com.catalyst.payment.infrastructure.stripe;

import com.catalyst.payment.application.ports.output.StripeGateway;
import com.catalyst.payment.domain.exception.PaymentException;
import com.catalyst.payment.domain.model.BillingCycle;
import com.catalyst.payment.domain.model.SubscriptionTier;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Adapter implementing StripeGateway using Stripe Java SDK.
 */
@Slf4j
@Component
public class StripeGatewayAdapter implements StripeGateway {

    @Value("${stripe.api-key}")
    private String apiKey;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    // Price IDs should be configured in application.yml
    @Value("${stripe.prices.professional-monthly:price_professional_monthly}")
    private String professionalMonthlyPriceId;

    @Value("${stripe.prices.professional-annual:price_professional_annual}")
    private String professionalAnnualPriceId;

    @Value("${stripe.prices.clinic-monthly:price_clinic_monthly}")
    private String clinicMonthlyPriceId;

    @Value("${stripe.prices.clinic-annual:price_clinic_annual}")
    private String clinicAnnualPriceId;

    @PostConstruct
    public void init() {
        Stripe.apiKey = apiKey;
        log.info("Stripe SDK initialized");
    }

    @Override
    public String createCustomer(String email, String name) {
        try {
            var params = com.stripe.param.CustomerCreateParams.builder()
                .setEmail(email)
                .setName(name)
                .build();

            var customer = com.stripe.model.Customer.create(params);
            log.info("Created Stripe customer: {}", customer.getId());
            return customer.getId();

        } catch (StripeException e) {
            log.error("Error creating Stripe customer", e);
            throw new PaymentException("STRIPE.CUSTOMER_CREATE_FAILED", 
                "Failed to create Stripe customer: " + e.getMessage(), e);
        }
    }

    @Override
    public String createCheckoutSession(String stripeCustomerId, SubscriptionTier tier,
                                         BillingCycle billingCycle, String successUrl, String cancelUrl) {
        try {
            String priceId = getPriceId(tier, billingCycle);

            var params = SessionCreateParams.builder()
                .setCustomer(stripeCustomerId)
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .addLineItem(
                    SessionCreateParams.LineItem.builder()
                        .setPrice(priceId)
                        .setQuantity(1L)
                        .build()
                )
                .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(cancelUrl)
                .setSubscriptionData(
                    SessionCreateParams.SubscriptionData.builder()
                        .putMetadata("tier", tier.name())
                        .putMetadata("billing_cycle", billingCycle.name())
                        .build()
                )
                .build();

            Session session = Session.create(params);
            log.info("Created checkout session: {}", session.getId());
            return session.getUrl();

        } catch (StripeException e) {
            log.error("Error creating checkout session", e);
            throw new PaymentException("STRIPE.CHECKOUT_CREATE_FAILED", 
                "Failed to create checkout session: " + e.getMessage(), e);
        }
    }

    @Override
    public String createCustomerPortalSession(String stripeCustomerId, String returnUrl) {
        try {
            var params = com.stripe.param.billingportal.SessionCreateParams.builder()
                .setCustomer(stripeCustomerId)
                .setReturnUrl(returnUrl)
                .build();

            com.stripe.model.billingportal.Session session = 
                com.stripe.model.billingportal.Session.create(params);
            log.info("Created customer portal session for customer: {}", stripeCustomerId);
            return session.getUrl();

        } catch (StripeException e) {
            log.error("Error creating customer portal session", e);
            throw new PaymentException("STRIPE.PORTAL_CREATE_FAILED", 
                "Failed to create customer portal session: " + e.getMessage(), e);
        }
    }

    @Override
    public void cancelSubscription(String stripeSubscriptionId, boolean immediate) {
        try {
            var subscription = com.stripe.model.Subscription.retrieve(stripeSubscriptionId);

            if (immediate) {
                subscription.cancel();
                log.info("Immediately canceled subscription: {}", stripeSubscriptionId);
            } else {
                var params = com.stripe.param.SubscriptionUpdateParams.builder()
                    .setCancelAtPeriodEnd(true)
                    .build();
                subscription.update(params);
                log.info("Scheduled subscription cancellation at period end: {}", stripeSubscriptionId);
            }

        } catch (StripeException e) {
            log.error("Error canceling subscription", e);
            throw new PaymentException("STRIPE.SUBSCRIPTION_CANCEL_FAILED", 
                "Failed to cancel subscription: " + e.getMessage(), e);
        }
    }

    @Override
    public StripeSubscriptionDetails getSubscription(String stripeSubscriptionId) {
        try {
            var subscription = com.stripe.model.Subscription.retrieve(stripeSubscriptionId);
            var item = subscription.getItems().getData().get(0);

            return new StripeSubscriptionDetails(
                subscription.getId(),
                subscription.getCustomer(),
                subscription.getStatus(),
                item.getPrice().getId(),
                subscription.getCurrentPeriodStart(),
                subscription.getCurrentPeriodEnd(),
                subscription.getTrialEnd(),
                subscription.getCancelAtPeriodEnd()
            );

        } catch (StripeException e) {
            log.error("Error retrieving subscription", e);
            throw new PaymentException("STRIPE.SUBSCRIPTION_RETRIEVE_FAILED", 
                "Failed to retrieve subscription: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean verifyWebhookSignature(String payload, String signature) {
        try {
            Webhook.constructEvent(payload, signature, webhookSecret);
            return true;
        } catch (SignatureVerificationException e) {
            log.warn("Invalid webhook signature");
            return false;
        }
    }

    @Override
    public StripeWebhookEvent parseWebhookEvent(String payload, String signature) {
        try {
            Event event = Webhook.constructEvent(payload, signature, webhookSecret);
            // Use EventDataObjectDeserializer instead of deprecated getData().getObject()
            Object dataObject = event.getDataObjectDeserializer()
                .getObject()
                .orElse(null);
            return new StripeWebhookEvent(
                event.getId(),
                event.getType(),
                dataObject
            );
        } catch (SignatureVerificationException e) {
            log.error("Failed to parse webhook event", e);
            throw new PaymentException("STRIPE.WEBHOOK_PARSE_FAILED", 
                "Failed to parse webhook event", e);
        }
    }

    private String getPriceId(SubscriptionTier tier, BillingCycle cycle) {
        return switch (tier) {
            case PROFESSIONAL -> cycle == BillingCycle.MONTHLY 
                ? professionalMonthlyPriceId 
                : professionalAnnualPriceId;
            case CLINIC -> cycle == BillingCycle.MONTHLY 
                ? clinicMonthlyPriceId 
                : clinicAnnualPriceId;
            case FREE_TRIAL -> throw new PaymentException("STRIPE.INVALID_TIER", 
                "Cannot create checkout for FREE_TRIAL tier");
        };
    }
}

