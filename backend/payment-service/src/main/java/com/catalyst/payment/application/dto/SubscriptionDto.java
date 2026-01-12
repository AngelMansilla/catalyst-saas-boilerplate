package com.catalyst.payment.application.dto;

import com.catalyst.payment.domain.model.BillingCycle;
import com.catalyst.payment.domain.model.CancellationReason;
import com.catalyst.payment.domain.model.Subscription;
import com.catalyst.payment.domain.model.SubscriptionStatus;
import com.catalyst.payment.domain.model.SubscriptionTier;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for subscription information.
 */
@Builder
public record SubscriptionDto(
    UUID id,
    UUID customerId,
    String stripeSubscriptionId,
    SubscriptionStatus status,
    SubscriptionTier tier,
    BillingCycle billingCycle,
    LocalDateTime trialEndDate,
    LocalDateTime currentPeriodStart,
    LocalDateTime currentPeriodEnd,
    LocalDateTime canceledAt,
    CancellationReason cancellationReason,
    boolean inTrial,
    boolean active,
    boolean canReactivate,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    /**
     * Creates a DTO from a Subscription domain entity.
     *
     * @param subscription the subscription entity
     * @return the DTO
     */
    public static SubscriptionDto fromEntity(Subscription subscription) {
        return SubscriptionDto.builder()
            .id(subscription.getId())
            .customerId(subscription.getCustomerId())
            .stripeSubscriptionId(subscription.getStripeSubscriptionId() != null 
                ? subscription.getStripeSubscriptionId().getValue() : null)
            .status(subscription.getStatus())
            .tier(subscription.getTier())
            .billingCycle(subscription.getBillingCycle())
            .trialEndDate(subscription.getTrialEndDate())
            .currentPeriodStart(subscription.getCurrentPeriodStart())
            .currentPeriodEnd(subscription.getCurrentPeriodEnd())
            .canceledAt(subscription.getCanceledAt())
            .cancellationReason(subscription.getCancellationReason())
            .inTrial(subscription.isInTrial())
            .active(subscription.isActive())
            .canReactivate(subscription.canReactivate())
            .createdAt(subscription.getCreatedAt())
            .updatedAt(subscription.getUpdatedAt())
            .build();
    }
}

