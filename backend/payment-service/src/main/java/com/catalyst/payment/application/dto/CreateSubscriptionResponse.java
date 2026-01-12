package com.catalyst.payment.application.dto;

import lombok.Builder;

import java.util.UUID;

/**
 * Response DTO for subscription checkout session creation.
 */
@Builder
public record CreateSubscriptionResponse(
    UUID customerId,
    String checkoutUrl,
    String stripeCustomerId
) {}

