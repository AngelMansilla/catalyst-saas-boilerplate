package com.catalyst.payment.application.dto;

import com.catalyst.payment.domain.model.CancellationReason;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

/**
 * Request DTO for canceling a subscription.
 */
@Builder
public record CancelSubscriptionRequest(
    @NotNull(message = "Subscription ID is required")
    UUID subscriptionId,

    @NotNull(message = "Cancellation reason is required")
    CancellationReason reason,

    boolean immediate
) {}

