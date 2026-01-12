package com.catalyst.payment.application.ports.input;

/**
 * Input port for processing Stripe webhooks.
 */
public interface ProcessWebhookUseCase {
    
    /**
     * Processes a Stripe webhook event.
     *
     * @param payload the raw webhook payload
     * @param signature the Stripe signature header
     * @return the result of processing
     */
    WebhookResult execute(String payload, String signature);

    /**
     * Webhook processing result.
     */
    record WebhookResult(
        boolean success,
        String eventId,
        String eventType,
        String message
    ) {
        public static WebhookResult success(String eventId, String eventType) {
            return new WebhookResult(true, eventId, eventType, "Event processed successfully");
        }

        public static WebhookResult alreadyProcessed(String eventId) {
            return new WebhookResult(true, eventId, null, "Event already processed");
        }

        public static WebhookResult failure(String message) {
            return new WebhookResult(false, null, null, message);
        }
    }
}

