package com.catalyst.payment.infrastructure.persistence.mapper;

import com.catalyst.payment.domain.model.Subscription;
import com.catalyst.payment.domain.valueobject.StripeSubscriptionId;
import com.catalyst.payment.infrastructure.persistence.entity.SubscriptionJpaEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper between Subscription domain entity and JPA entity.
 */
@Component
public class SubscriptionMapper {

    /**
     * Maps JPA entity to domain entity.
     *
     * @param entity the JPA entity
     * @return the domain entity
     */
    public Subscription toDomain(SubscriptionJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        // Create base subscription using factory method
        // Then override with persisted values
        Subscription subscription = Subscription.startTrial(
            entity.getCustomerId(),
            entity.getTier(),
            14 // Default trial days, will be overwritten
        );

        // Override all values from persisted entity
        subscription.setId(entity.getId());
        subscription.setCustomerId(entity.getCustomerId());
        subscription.setStatus(entity.getStatus());
        subscription.setTier(entity.getTier());
        subscription.setBillingCycle(entity.getBillingCycle());
        subscription.setTrialEndDate(entity.getTrialEndDate());
        subscription.setCurrentPeriodStart(entity.getCurrentPeriodStart());
        subscription.setCurrentPeriodEnd(entity.getCurrentPeriodEnd());
        subscription.setCanceledAt(entity.getCanceledAt());
        subscription.setCancellationReason(entity.getCancellationReason());
        subscription.setCreatedAt(entity.getCreatedAt());
        subscription.setUpdatedAt(entity.getUpdatedAt());

        if (entity.getStripeSubscriptionId() != null) {
            subscription.setStripeSubscriptionId(
                StripeSubscriptionId.of(entity.getStripeSubscriptionId())
            );
        }

        return subscription;
    }

    /**
     * Maps domain entity to JPA entity.
     *
     * @param subscription the domain entity
     * @return the JPA entity
     */
    public SubscriptionJpaEntity toJpaEntity(Subscription subscription) {
        if (subscription == null) {
            return null;
        }

        SubscriptionJpaEntity entity = new SubscriptionJpaEntity();
        entity.setId(subscription.getId());
        entity.setCustomerId(subscription.getCustomerId());
        entity.setStatus(subscription.getStatus());
        entity.setTier(subscription.getTier());
        entity.setBillingCycle(subscription.getBillingCycle());
        entity.setTrialEndDate(subscription.getTrialEndDate());
        entity.setCurrentPeriodStart(subscription.getCurrentPeriodStart());
        entity.setCurrentPeriodEnd(subscription.getCurrentPeriodEnd());
        entity.setCanceledAt(subscription.getCanceledAt());
        entity.setCancellationReason(subscription.getCancellationReason());
        entity.setCreatedAt(subscription.getCreatedAt());
        entity.setUpdatedAt(subscription.getUpdatedAt());

        if (subscription.getStripeSubscriptionId() != null) {
            entity.setStripeSubscriptionId(subscription.getStripeSubscriptionId().getValue());
        }

        return entity;
    }
}

