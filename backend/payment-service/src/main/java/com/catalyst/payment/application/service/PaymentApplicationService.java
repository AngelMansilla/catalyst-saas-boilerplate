package com.catalyst.payment.application.service;

import com.catalyst.payment.application.dto.*;
import com.catalyst.payment.application.ports.input.*;
import com.catalyst.payment.application.ports.output.*;
import com.catalyst.payment.domain.event.*;
import com.catalyst.payment.domain.exception.PaymentException;
import com.catalyst.payment.domain.exception.SubscriptionNotFoundException;
import com.catalyst.payment.domain.model.*;
import com.catalyst.payment.domain.valueobject.StripeSubscriptionId;
import com.catalyst.shared.domain.common.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Application service that orchestrates payment use cases.
 * Implements all input ports and coordinates domain entities with output ports.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentApplicationService implements 
        CreateSubscriptionUseCase, 
        GetSubscriptionUseCase, 
        CancelSubscriptionUseCase,
        GetCustomerPortalUrlUseCase,
        ProcessWebhookUseCase {

    private final CustomerRepository customerRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final WebhookEventRepository webhookEventRepository;
    private final StripeGateway stripeGateway;
    private final EventPublisher eventPublisher;
    private final int trialDurationDays;

    // ==================== CreateSubscriptionUseCase ====================

    @Override
    @Transactional
    public CreateSubscriptionResponse execute(CreateSubscriptionRequest request) {
        log.info("Creating subscription for user: {}, tier: {}, cycle: {}", 
            request.userId(), request.tier(), request.billingCycle());

        // Check if user already has an active subscription
        if (subscriptionRepository.existsActiveByCustomerId(request.userId())) {
            throw new PaymentException("User already has an active subscription");
        }

        // Get or create customer
        Customer customer = customerRepository.findByUserId(request.userId())
            .orElseGet(() -> createCustomer(request));

        // Ensure customer has Stripe ID
        if (customer.getStripeCustomerId() == null) {
            String stripeCustomerId = stripeGateway.createCustomer(
                request.email(), 
                request.name()
            );
            customer.associateStripeCustomer(
                com.catalyst.payment.domain.valueobject.StripeCustomerId.of(stripeCustomerId)
            );
            customer = customerRepository.save(customer);
        }

        // Create checkout session
        String checkoutUrl = stripeGateway.createCheckoutSession(
            customer.getStripeCustomerId().getValue(),
            request.tier(),
            request.billingCycle(),
            request.successUrl(),
            request.cancelUrl()
        );

        log.info("Checkout session created for customer: {}", customer.getId());

        return CreateSubscriptionResponse.builder()
            .customerId(customer.getId())
            .checkoutUrl(checkoutUrl)
            .stripeCustomerId(customer.getStripeCustomerId().getValue())
            .build();
    }

    private Customer createCustomer(CreateSubscriptionRequest request) {
        Customer customer = Customer.create(
            request.userId(),
            Email.of(request.email()),
            request.name()
        );
        return customerRepository.save(customer);
    }

    // ==================== GetSubscriptionUseCase ====================

    @Override
    @Transactional(readOnly = true)
    public SubscriptionDto getById(UUID subscriptionId) {
        log.debug("Getting subscription by ID: {}", subscriptionId);
        
        return subscriptionRepository.findById(subscriptionId)
            .map(SubscriptionDto::fromEntity)
            .orElseThrow(() -> new SubscriptionNotFoundException(subscriptionId));
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionDto getActiveByUserId(UUID userId) {
        log.debug("Getting active subscription for user: {}", userId);
        
        Customer customer = customerRepository.findByUserId(userId)
            .orElse(null);
        
        if (customer == null) {
            return null;
        }

        return subscriptionRepository.findActiveByCustomerId(customer.getId())
            .map(SubscriptionDto::fromEntity)
            .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionDto> getAllByUserId(UUID userId) {
        log.debug("Getting all subscriptions for user: {}", userId);
        
        Customer customer = customerRepository.findByUserId(userId)
            .orElse(null);
        
        if (customer == null) {
            return List.of();
        }

        return subscriptionRepository.findByCustomerId(customer.getId())
            .stream()
            .map(SubscriptionDto::fromEntity)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasActiveSubscription(UUID userId) {
        Customer customer = customerRepository.findByUserId(userId)
            .orElse(null);
        
        if (customer == null) {
            return false;
        }

        return subscriptionRepository.existsActiveByCustomerId(customer.getId());
    }

    // ==================== CancelSubscriptionUseCase ====================

    @Override
    @Transactional
    public SubscriptionDto execute(CancelSubscriptionRequest request) {
        log.info("Canceling subscription: {}, reason: {}, immediate: {}", 
            request.subscriptionId(), request.reason(), request.immediate());

        Subscription subscription = subscriptionRepository.findById(request.subscriptionId())
            .orElseThrow(() -> new SubscriptionNotFoundException(request.subscriptionId()));

        // Cancel in Stripe first
        if (subscription.getStripeSubscriptionId() != null) {
            stripeGateway.cancelSubscription(
                subscription.getStripeSubscriptionId().getValue(),
                request.immediate()
            );
        }

        // Cancel in domain
        subscription.cancel(request.reason(), request.immediate());
        subscription = subscriptionRepository.save(subscription);

        // Publish event
        SubscriptionCanceled event = SubscriptionCanceled.of(
            subscription.getId(),
            subscription.getCustomerId(),
            request.reason(),
            request.immediate(),
            subscription.getCurrentPeriodEnd()
        );
        eventPublisher.publish(event, UUID.randomUUID().toString());

        log.info("Subscription canceled: {}", subscription.getId());

        return SubscriptionDto.fromEntity(subscription);
    }

    // ==================== GetCustomerPortalUrlUseCase ====================

    @Override
    @Transactional(readOnly = true)
    public String execute(UUID userId, String returnUrl) {
        log.debug("Getting customer portal URL for user: {}", userId);

        Customer customer = customerRepository.findByUserId(userId)
            .orElseThrow(() -> new PaymentException("Customer not found for user: " + userId));

        if (customer.getStripeCustomerId() == null) {
            throw new PaymentException("Customer has no Stripe account");
        }

        return stripeGateway.createCustomerPortalSession(
            customer.getStripeCustomerId().getValue(),
            returnUrl
        );
    }

    // ==================== ProcessWebhookUseCase ====================

    @Override
    @Transactional
    public WebhookResult execute(String payload, String signature) {
        log.debug("Processing webhook event");

        // Verify signature
        if (!stripeGateway.verifyWebhookSignature(payload, signature)) {
            log.warn("Invalid webhook signature");
            return WebhookResult.failure("Invalid signature");
        }

        // Parse event
        StripeGateway.StripeWebhookEvent event = stripeGateway.parseWebhookEvent(payload, signature);

        // Check idempotency
        if (webhookEventRepository.existsByStripeEventId(event.id())) {
            log.info("Event already processed: {}", event.id());
            return WebhookResult.alreadyProcessed(event.id());
        }

        try {
            // Process based on event type
            processEventByType(event);

            // Record processed event
            webhookEventRepository.recordProcessedEvent(event.id(), event.type(), payload);

            log.info("Webhook processed successfully: {} ({})", event.id(), event.type());
            return WebhookResult.success(event.id(), event.type());

        } catch (Exception e) {
            log.error("Error processing webhook: {} - {}", event.id(), e.getMessage(), e);
            return WebhookResult.failure(e.getMessage());
        }
    }

    private void processEventByType(StripeGateway.StripeWebhookEvent event) {
        switch (event.type()) {
            case "checkout.session.completed" -> handleCheckoutSessionCompleted(event);
            case "invoice.paid" -> handleInvoicePaid(event);
            case "invoice.payment_failed" -> handleInvoicePaymentFailed(event);
            case "customer.subscription.deleted" -> handleSubscriptionDeleted(event);
            case "customer.subscription.updated" -> handleSubscriptionUpdated(event);
            default -> log.info("Unhandled event type: {}", event.type());
        }
    }

    private void handleCheckoutSessionCompleted(StripeGateway.StripeWebhookEvent event) {
        log.info("Handling checkout.session.completed");
        // Implementation will extract subscription details from event data
        // and create/activate the subscription
    }

    private void handleInvoicePaid(StripeGateway.StripeWebhookEvent event) {
        log.info("Handling invoice.paid");
        // Implementation will mark invoice as paid and update subscription status
    }

    private void handleInvoicePaymentFailed(StripeGateway.StripeWebhookEvent event) {
        log.info("Handling invoice.payment_failed");
        // Implementation will mark subscription as past due and publish PaymentFailed event
    }

    private void handleSubscriptionDeleted(StripeGateway.StripeWebhookEvent event) {
        log.info("Handling customer.subscription.deleted");
        // Implementation will cancel the subscription
    }

    private void handleSubscriptionUpdated(StripeGateway.StripeWebhookEvent event) {
        log.info("Handling customer.subscription.updated");
        // Implementation will update subscription status based on Stripe status
    }

    // ==================== Helper Methods ====================

    /**
     * Creates a trial subscription for a customer.
     * Called after checkout session is completed.
     */
    @Transactional
    public Subscription createTrialSubscription(UUID customerId, SubscriptionTier tier) {
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new PaymentException("Customer not found: " + customerId));

        Subscription subscription = Subscription.startTrial(customerId, tier, trialDurationDays);
        subscription = subscriptionRepository.save(subscription);

        // Publish event
        SubscriptionCreated event = SubscriptionCreated.of(
            subscription.getId(),
            customer.getUserId(),
            tier,
            subscription.getTrialEndDate()
        );
        eventPublisher.publish(event, UUID.randomUUID().toString());

        log.info("Trial subscription created: {} for customer: {}", subscription.getId(), customerId);

        return subscription;
    }

    /**
     * Activates a subscription after successful payment.
     */
    @Transactional
    public Subscription activateSubscription(UUID subscriptionId, String stripeSubscriptionId, 
                                              BillingCycle billingCycle) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
            .orElseThrow(() -> new SubscriptionNotFoundException(subscriptionId));

        subscription.activate(StripeSubscriptionId.of(stripeSubscriptionId), billingCycle);
        subscription = subscriptionRepository.save(subscription);

        // Get customer for event
        Customer customer = customerRepository.findById(subscription.getCustomerId())
            .orElseThrow(() -> new PaymentException("Customer not found"));

        // Publish event
        SubscriptionActivated event = SubscriptionActivated.of(
            subscription.getId(),
            customer.getUserId(),
            subscription.getTier(),
            billingCycle,
            stripeSubscriptionId,
            subscription.getCurrentPeriodEnd()
        );
        eventPublisher.publish(event, UUID.randomUUID().toString());

        log.info("Subscription activated: {}", subscription.getId());

        return subscription;
    }
}

