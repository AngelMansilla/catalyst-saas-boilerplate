package com.catalyst.payment.application.dto;

import com.catalyst.payment.domain.model.BillingCycle;
import com.catalyst.payment.domain.model.SubscriptionTier;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

/**
 * Request DTO for creating a subscription checkout session.
 */
@Builder
public record CreateSubscriptionRequest(
    @NotNull(message = "User ID is required")
    UUID userId,

    @NotBlank(message = "Email is required")
    String email,

    String name,

    @NotNull(message = "Subscription tier is required")
    SubscriptionTier tier,

    @NotNull(message = "Billing cycle is required")
    BillingCycle billingCycle,

    @NotBlank(message = "Success URL is required")
    String successUrl,

    @NotBlank(message = "Cancel URL is required")
    String cancelUrl
) {}

