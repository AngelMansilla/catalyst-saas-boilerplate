package com.catalyst.payment.application.ports.output;

import java.util.Optional;
import java.util.UUID;

/**
 * Output port for webhook event idempotency.
 */
public interface WebhookEventRepository {
    
    /**
     * Checks if an event has already been processed.
     *
     * @param stripeEventId the Stripe event ID
     * @return true if the event has been processed
     */
    boolean existsByStripeEventId(String stripeEventId);

    /**
     * Records a processed event.
     *
     * @param stripeEventId the Stripe event ID
     * @param eventType the event type
     * @param payload the event payload as JSON
     * @return the generated record ID
     */
    UUID recordProcessedEvent(String stripeEventId, String eventType, String payload);

    /**
     * Finds the payload of a processed event.
     *
     * @param stripeEventId the Stripe event ID
     * @return the event payload if found
     */
    Optional<String> findPayloadByStripeEventId(String stripeEventId);
}

