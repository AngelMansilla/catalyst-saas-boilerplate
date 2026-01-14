package com.catalyst.notification.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for NotificationType enum.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
@DisplayName("NotificationType Enum")
class NotificationTypeTest {
    
    @Nested
    @DisplayName("Code and Template Name")
    class CodeAndTemplateName {
        
        @Test
        @DisplayName("Should return correct code for each type")
        void shouldReturnCorrectCodeForEachType() {
            assertEquals("welcome", NotificationType.WELCOME.getCode());
            assertEquals("password-reset", NotificationType.PASSWORD_RESET.getCode());
            assertEquals("password-reset-confirmation", NotificationType.PASSWORD_RESET_CONFIRMATION.getCode());
            assertEquals("subscription-created", NotificationType.SUBSCRIPTION_CREATED.getCode());
            assertEquals("payment-receipt", NotificationType.PAYMENT_RECEIPT.getCode());
            assertEquals("payment-failed", NotificationType.PAYMENT_FAILED.getCode());
            assertEquals("subscription-canceled", NotificationType.SUBSCRIPTION_CANCELED.getCode());
            assertEquals("trial-expired", NotificationType.TRIAL_EXPIRED.getCode());
        }
        
        @Test
        @DisplayName("Should return correct template name for each type")
        void shouldReturnCorrectTemplateNameForEachType() {
            assertEquals("welcome", NotificationType.WELCOME.getTemplateName());
            assertEquals("passwordreset", NotificationType.PASSWORD_RESET.getTemplateName());
            assertEquals("passwordresetconfirmation", NotificationType.PASSWORD_RESET_CONFIRMATION.getTemplateName());
            assertEquals("subscriptioncreated", NotificationType.SUBSCRIPTION_CREATED.getTemplateName());
            assertEquals("paymentreceipt", NotificationType.PAYMENT_RECEIPT.getTemplateName());
            assertEquals("paymentfailed", NotificationType.PAYMENT_FAILED.getTemplateName());
            assertEquals("subscriptioncanceled", NotificationType.SUBSCRIPTION_CANCELED.getTemplateName());
            assertEquals("trialexpired", NotificationType.TRIAL_EXPIRED.getTemplateName());
        }
    }
    
    @Nested
    @DisplayName("fromCode")
    class FromCode {
        
        @Test
        @DisplayName("Should return correct type for valid code")
        void shouldReturnCorrectTypeForValidCode() {
            assertEquals(NotificationType.WELCOME, NotificationType.fromCode("welcome"));
            assertEquals(NotificationType.PASSWORD_RESET, NotificationType.fromCode("password-reset"));
            assertEquals(NotificationType.SUBSCRIPTION_CREATED, NotificationType.fromCode("subscription-created"));
        }
        
        @Test
        @DisplayName("Should throw exception for invalid code")
        void shouldThrowExceptionForInvalidCode() {
            assertThrows(IllegalArgumentException.class, () -> {
                NotificationType.fromCode("invalid-type");
            });
        }
        
        @Test
        @DisplayName("Should throw exception for null code")
        void shouldThrowExceptionForNullCode() {
            assertThrows(IllegalArgumentException.class, () -> {
                NotificationType.fromCode(null);
            });
        }
    }
}

