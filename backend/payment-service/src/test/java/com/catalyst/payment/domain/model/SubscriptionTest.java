package com.catalyst.payment.domain.model;

import com.catalyst.payment.domain.exception.InvalidSubscriptionStateException;
import com.catalyst.payment.domain.valueobject.StripeSubscriptionId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for Subscription aggregate root and state machine.
 */
@DisplayName("Subscription")
class SubscriptionTest {

    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final int TRIAL_DAYS = 14;

    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethods {

        @Test
        @DisplayName("startTrial_whenValidInput_thenCreatesSubscriptionInTrialStatus")
        void startTrial_whenValidInput_thenCreatesSubscriptionInTrialStatus() {
            Subscription subscription = Subscription.startTrial(CUSTOMER_ID, SubscriptionTier.PROFESSIONAL, TRIAL_DAYS);

            assertThat(subscription.getId()).isNotNull();
            assertThat(subscription.getCustomerId()).isEqualTo(CUSTOMER_ID);
            assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.TRIAL);
            assertThat(subscription.getTier()).isEqualTo(SubscriptionTier.PROFESSIONAL);
            assertThat(subscription.getTrialEndDate()).isAfter(LocalDateTime.now());
            assertThat(subscription.isInTrial()).isTrue();
        }

        @Test
        @DisplayName("startTrial_whenNullCustomerId_thenThrowsIllegalArgument")
        void startTrial_whenNullCustomerId_thenThrowsIllegalArgument() {
            assertThatThrownBy(() -> Subscription.startTrial(null, SubscriptionTier.PROFESSIONAL, TRIAL_DAYS))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Customer ID");
        }

        @Test
        @DisplayName("startTrial_whenNullTier_thenThrowsIllegalArgument")
        void startTrial_whenNullTier_thenThrowsIllegalArgument() {
            assertThatThrownBy(() -> Subscription.startTrial(CUSTOMER_ID, null, TRIAL_DAYS))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tier");
        }

        @Test
        @DisplayName("startTrial_whenNonPositiveTrialDays_thenThrowsIllegalArgument")
        void startTrial_whenNonPositiveTrialDays_thenThrowsIllegalArgument() {
            assertThatThrownBy(() -> Subscription.startTrial(CUSTOMER_ID, SubscriptionTier.PROFESSIONAL, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Trial days");
        }
    }

    @Nested
    @DisplayName("State Transitions")
    class StateTransitions {

        @Test
        @DisplayName("activate_whenTrialSubscription_thenTransitionsToActive")
        void activate_whenTrialSubscription_thenTransitionsToActive() {
            Subscription subscription = Subscription.startTrial(CUSTOMER_ID, SubscriptionTier.PROFESSIONAL, TRIAL_DAYS);
            StripeSubscriptionId stripeId = StripeSubscriptionId.of("sub_123456");

            subscription.activate(stripeId, BillingCycle.MONTHLY);

            assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
            assertThat(subscription.getStripeSubscriptionId()).isEqualTo(stripeId);
            assertThat(subscription.getBillingCycle()).isEqualTo(BillingCycle.MONTHLY);
            assertThat(subscription.getTrialEndDate()).isNull();
            assertThat(subscription.getCurrentPeriodEnd()).isNotNull();
            assertThat(subscription.isActive()).isTrue();
            assertThat(subscription.isInTrial()).isFalse();
        }

        @Test
        @DisplayName("cancel_whenTrialSubscription_thenTransitionsToCanceled")
        void cancel_whenTrialSubscription_thenTransitionsToCanceled() {
            Subscription subscription = Subscription.startTrial(CUSTOMER_ID, SubscriptionTier.PROFESSIONAL, TRIAL_DAYS);

            subscription.cancel(CancellationReason.USER_REQUESTED, true);

            assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.CANCELED);
            assertThat(subscription.getCancellationReason()).isEqualTo(CancellationReason.USER_REQUESTED);
            assertThat(subscription.getCanceledAt()).isNotNull();
            assertThat(subscription.isCanceled()).isTrue();
        }

        @Test
        @DisplayName("expireTrial_whenTrialSubscription_thenTransitionsToExpired")
        void expireTrial_whenTrialSubscription_thenTransitionsToExpired() {
            Subscription subscription = Subscription.startTrial(CUSTOMER_ID, SubscriptionTier.PROFESSIONAL, TRIAL_DAYS);
            // Manually set trial end to past
            subscription.setTrialEndDate(LocalDateTime.now().minusDays(1));

            subscription.expireTrial();

            assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.EXPIRED);
        }

        @Test
        @DisplayName("markPastDue_whenActiveSubscription_thenTransitionsToPastDue")
        void markPastDue_whenActiveSubscription_thenTransitionsToPastDue() {
            Subscription subscription = createActiveSubscription();

            subscription.markPastDue();

            assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.PAST_DUE);
            assertThat(subscription.isPastDue()).isTrue();
        }

        @Test
        @DisplayName("cancel_whenActiveSubscription_thenTransitionsToCanceled")
        void cancel_whenActiveSubscription_thenTransitionsToCanceled() {
            Subscription subscription = createActiveSubscription();

            subscription.cancel(CancellationReason.USER_REQUESTED, false);

            assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.CANCELED);
        }

        @Test
        @DisplayName("recordPaymentSuccess_whenPastDueSubscription_thenTransitionsToActive")
        void recordPaymentSuccess_whenPastDueSubscription_thenTransitionsToActive() {
            Subscription subscription = createActiveSubscription();
            subscription.markPastDue();

            subscription.recordPaymentSuccess();

            assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        }

        @Test
        @DisplayName("cancel_whenPastDueSubscription_thenTransitionsToCanceled")
        void cancel_whenPastDueSubscription_thenTransitionsToCanceled() {
            Subscription subscription = createActiveSubscription();
            subscription.markPastDue();

            subscription.cancel(CancellationReason.PAYMENT_FAILED, true);

            assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.CANCELED);
        }
    }

    @Nested
    @DisplayName("Invalid State Transitions")
    class InvalidStateTransitions {

        @Test
        @DisplayName("markPastDue_whenTrialSubscription_thenThrowsInvalidSubscriptionState")
        void markPastDue_whenTrialSubscription_thenThrowsInvalidSubscriptionState() {
            Subscription subscription = Subscription.startTrial(CUSTOMER_ID, SubscriptionTier.PROFESSIONAL, TRIAL_DAYS);

            assertThatThrownBy(subscription::markPastDue)
                .isInstanceOf(InvalidSubscriptionStateException.class)
                .hasMessageContaining("TRIAL")
                .hasMessageContaining("PAST_DUE");
        }

        @Test
        @DisplayName("activate_whenCanceledSubscription_thenReactivatesToActive")
        void activate_whenCanceledSubscription_thenReactivatesToActive() {
            Subscription subscription = Subscription.startTrial(CUSTOMER_ID, SubscriptionTier.PROFESSIONAL, TRIAL_DAYS);
            subscription.cancel(CancellationReason.USER_REQUESTED, true);

            // CANCELED allows transition to ACTIVE for reactivation
            subscription.activate(StripeSubscriptionId.of("sub_123"), BillingCycle.MONTHLY);
            
            assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
            assertThat(subscription.canReactivate()).isFalse(); // No longer reactivatable
        }

        @Test
        @DisplayName("expireTrial_whenAlreadyExpired_thenThrowsInvalidSubscriptionState")
        void expireTrial_whenAlreadyExpired_thenThrowsInvalidSubscriptionState() {
            Subscription subscription = Subscription.startTrial(CUSTOMER_ID, SubscriptionTier.PROFESSIONAL, TRIAL_DAYS);
            subscription.setTrialEndDate(LocalDateTime.now().minusDays(1));
            subscription.expireTrial();

            assertThatThrownBy(subscription::expireTrial)
                .isInstanceOf(InvalidSubscriptionStateException.class)
                .hasMessageContaining("TRIAL");
        }
    }

    @Nested
    @DisplayName("Business Logic")
    class BusinessLogic {

        @Test
        @DisplayName("isInTrial_whenTrialNotExpired_thenReturnsTrueOtherwiseFalse")
        void isInTrial_whenTrialNotExpired_thenReturnsTrueOtherwiseFalse() {
            Subscription subscription = Subscription.startTrial(CUSTOMER_ID, SubscriptionTier.PROFESSIONAL, TRIAL_DAYS);
            
            assertThat(subscription.isInTrial()).isTrue();

            // After expiration
            subscription.setTrialEndDate(LocalDateTime.now().minusDays(1));
            assertThat(subscription.isInTrial()).isFalse();
        }

        @Test
        @DisplayName("canReactivate_whenCanceledOrExpired_thenReturnsTrueOtherwiseFalse")
        void canReactivate_whenCanceledOrExpired_thenReturnsTrueOtherwiseFalse() {
            Subscription canceledSub = Subscription.startTrial(CUSTOMER_ID, SubscriptionTier.PROFESSIONAL, TRIAL_DAYS);
            canceledSub.cancel(CancellationReason.USER_REQUESTED, true);
            assertThat(canceledSub.canReactivate()).isTrue();

            Subscription expiredSub = Subscription.startTrial(CUSTOMER_ID, SubscriptionTier.CLINIC, TRIAL_DAYS);
            expiredSub.setTrialEndDate(LocalDateTime.now().minusDays(1));
            expiredSub.expireTrial();
            assertThat(expiredSub.canReactivate()).isTrue();

            Subscription activeSub = createActiveSubscription();
            assertThat(activeSub.canReactivate()).isFalse();
        }

        @Test
        @DisplayName("updatePeriod_whenValidDates_thenUpdatesPeriodStartAndEnd")
        void updatePeriod_whenValidDates_thenUpdatesPeriodStartAndEnd() {
            Subscription subscription = createActiveSubscription();
            LocalDateTime start = LocalDateTime.now();
            LocalDateTime end = start.plusMonths(1);

            subscription.updatePeriod(start, end);

            assertThat(subscription.getCurrentPeriodStart()).isEqualTo(start);
            assertThat(subscription.getCurrentPeriodEnd()).isEqualTo(end);
        }

        @Test
        @DisplayName("updatePeriod_whenNullDates_thenThrowsIllegalArgument")
        void updatePeriod_whenNullDates_thenThrowsIllegalArgument() {
            Subscription subscription = createActiveSubscription();

            assertThatThrownBy(() -> subscription.updatePeriod(null, LocalDateTime.now()))
                .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> subscription.updatePeriod(LocalDateTime.now(), null))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("updatePeriod_whenStartAfterEnd_thenThrowsIllegalArgument")
        void updatePeriod_whenStartAfterEnd_thenThrowsIllegalArgument() {
            Subscription subscription = createActiveSubscription();
            LocalDateTime start = LocalDateTime.now().plusMonths(1);
            LocalDateTime end = LocalDateTime.now();

            assertThatThrownBy(() -> subscription.updatePeriod(start, end))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("before");
        }
    }

    @Nested
    @DisplayName("Subscription Status")
    class SubscriptionStatusTests {

        @Test
        @DisplayName("canTransitionTo_whenTrialStatus_thenAllowsActiveAndCanceledAndExpiredOnly")
        void canTransitionTo_whenTrialStatus_thenAllowsActiveAndCanceledAndExpiredOnly() {
            assertThat(SubscriptionStatus.TRIAL.canTransitionTo(SubscriptionStatus.ACTIVE)).isTrue();
            assertThat(SubscriptionStatus.TRIAL.canTransitionTo(SubscriptionStatus.CANCELED)).isTrue();
            assertThat(SubscriptionStatus.TRIAL.canTransitionTo(SubscriptionStatus.EXPIRED)).isTrue();
            assertThat(SubscriptionStatus.TRIAL.canTransitionTo(SubscriptionStatus.PAST_DUE)).isFalse();
        }

        @Test
        @DisplayName("canTransitionTo_whenActiveStatus_thenAllowsPastDueAndCanceledOnly")
        void canTransitionTo_whenActiveStatus_thenAllowsPastDueAndCanceledOnly() {
            assertThat(SubscriptionStatus.ACTIVE.canTransitionTo(SubscriptionStatus.PAST_DUE)).isTrue();
            assertThat(SubscriptionStatus.ACTIVE.canTransitionTo(SubscriptionStatus.CANCELED)).isTrue();
            assertThat(SubscriptionStatus.ACTIVE.canTransitionTo(SubscriptionStatus.TRIAL)).isFalse();
            assertThat(SubscriptionStatus.ACTIVE.canTransitionTo(SubscriptionStatus.EXPIRED)).isFalse();
        }

        @Test
        @DisplayName("canTransitionTo_whenPastDueStatus_thenAllowsActiveAndCanceledOnly")
        void canTransitionTo_whenPastDueStatus_thenAllowsActiveAndCanceledOnly() {
            assertThat(SubscriptionStatus.PAST_DUE.canTransitionTo(SubscriptionStatus.ACTIVE)).isTrue();
            assertThat(SubscriptionStatus.PAST_DUE.canTransitionTo(SubscriptionStatus.CANCELED)).isTrue();
            assertThat(SubscriptionStatus.PAST_DUE.canTransitionTo(SubscriptionStatus.TRIAL)).isFalse();
        }
    }

    // Helper method to create an active subscription
    private Subscription createActiveSubscription() {
        Subscription subscription = Subscription.startTrial(CUSTOMER_ID, SubscriptionTier.PROFESSIONAL, TRIAL_DAYS);
        subscription.activate(StripeSubscriptionId.of("sub_test123"), BillingCycle.MONTHLY);
        return subscription;
    }
}

