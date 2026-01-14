package com.catalyst.notification.domain.model;

import com.catalyst.notification.domain.valueobject.EmailAddress;
import com.catalyst.notification.domain.valueobject.NotificationStatus;
import com.catalyst.notification.domain.valueobject.NotificationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Notification aggregate root.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
@DisplayName("Notification Aggregate")
class NotificationTest {
    
    private static final String VALID_EMAIL = "test@example.com";
    private static final String VALID_SUBJECT = "Test Subject";
    
    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethods {
        
        @Test
        @DisplayName("Should create notification with all required fields")
        void shouldCreateNotificationWithAllRequiredFields() {
            // Given
            EmailAddress recipient = new EmailAddress(VALID_EMAIL);
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("userName", "John Doe");
            
            // When
            Notification notification = Notification.create(
                NotificationType.WELCOME,
                recipient,
                VALID_SUBJECT,
                templateData
            );
            
            // Then
            assertNotNull(notification.getId());
            assertEquals(NotificationType.WELCOME, notification.getType());
            assertEquals(recipient, notification.getRecipient());
            assertEquals(VALID_SUBJECT, notification.getSubject());
            assertEquals(NotificationStatus.PENDING, notification.getStatus());
            assertEquals(0, notification.getRetryCount());
            assertNull(notification.getErrorMessage());
            assertNotNull(notification.getCreatedAt());
            assertNotNull(notification.getUpdatedAt());
            assertNull(notification.getSentAt());
        }
        
        @Test
        @DisplayName("Should create notification with null template data")
        void shouldCreateNotificationWithNullTemplateData() {
            // Given
            EmailAddress recipient = new EmailAddress(VALID_EMAIL);
            
            // When
            Notification notification = Notification.create(
                NotificationType.PASSWORD_RESET,
                recipient,
                VALID_SUBJECT,
                null
            );
            
            // Then
            assertNotNull(notification);
            assertNotNull(notification.getTemplateData());
            assertTrue(notification.getTemplateData().isEmpty());
        }
        
        @Test
        @DisplayName("Should throw exception when type is null")
        void shouldThrowExceptionWhenTypeIsNull() {
            // Given
            EmailAddress recipient = new EmailAddress(VALID_EMAIL);
            
            // When & Then
            assertThrows(NullPointerException.class, () -> {
                Notification.create(null, recipient, VALID_SUBJECT, null);
            });
        }
        
        @Test
        @DisplayName("Should throw exception when recipient is null")
        void shouldThrowExceptionWhenRecipientIsNull() {
            // When & Then
            assertThrows(NullPointerException.class, () -> {
                Notification.create(NotificationType.WELCOME, null, VALID_SUBJECT, null);
            });
        }
        
        @Test
        @DisplayName("Should throw exception when subject is null")
        void shouldThrowExceptionWhenSubjectIsNull() {
            // Given
            EmailAddress recipient = new EmailAddress(VALID_EMAIL);
            
            // When & Then
            assertThrows(NullPointerException.class, () -> {
                Notification.create(NotificationType.WELCOME, recipient, null, null);
            });
        }
    }
    
    @Nested
    @DisplayName("Business Logic")
    class BusinessLogic {
        
        @Test
        @DisplayName("Should mark notification as sent")
        void shouldMarkNotificationAsSent() {
            // Given
            Notification notification = createTestNotification();
            
            // When
            notification.markAsSent();
            
            // Then
            assertEquals(NotificationStatus.SENT, notification.getStatus());
            assertNotNull(notification.getSentAt());
            assertNull(notification.getErrorMessage());
        }
        
        @Test
        @DisplayName("Should mark notification as failed and increment retry count")
        void shouldMarkNotificationAsFailed() {
            // Given
            Notification notification = createTestNotification();
            String errorMessage = "SMTP connection failed";
            
            // When
            notification.markAsFailed(errorMessage);
            
            // Then
            assertEquals(NotificationStatus.FAILED, notification.getStatus());
            assertEquals(errorMessage, notification.getErrorMessage());
            assertEquals(1, notification.getRetryCount());
        }
        
        @Test
        @DisplayName("Should mark notification as retrying")
        void shouldMarkNotificationAsRetrying() {
            // Given
            Notification notification = createTestNotification();
            notification.markAsFailed("First failure");
            
            // When
            notification.markAsRetrying();
            
            // Then
            assertEquals(NotificationStatus.RETRYING, notification.getStatus());
        }
        
        @Test
        @DisplayName("Should allow retry when retry count is below max")
        void shouldAllowRetryWhenRetryCountBelowMax() {
            // Given
            Notification notification = createTestNotification();
            notification.markAsFailed("Error");
            int maxRetries = 3;
            
            // When
            boolean canRetry = notification.canRetry(maxRetries);
            
            // Then
            assertTrue(canRetry);
        }
        
        @Test
        @DisplayName("Should not allow retry when retry count equals max")
        void shouldNotAllowRetryWhenRetryCountEqualsMax() {
            // Given
            Notification notification = createTestNotification();
            notification.markAsFailed("Error");
            notification.markAsFailed("Error");
            notification.markAsFailed("Error");
            int maxRetries = 3;
            
            // When
            boolean canRetry = notification.canRetry(maxRetries);
            
            // Then
            assertFalse(canRetry);
        }
        
        @Test
        @DisplayName("Should not allow retry when status is not FAILED")
        void shouldNotAllowRetryWhenStatusIsNotFailed() {
            // Given
            Notification notification = createTestNotification();
            notification.markAsSent();
            int maxRetries = 3;
            
            // When
            boolean canRetry = notification.canRetry(maxRetries);
            
            // Then
            assertFalse(canRetry);
        }
        
        @Test
        @DisplayName("Should increment retry count on multiple failures")
        void shouldIncrementRetryCountOnMultipleFailures() {
            // Given
            Notification notification = createTestNotification();
            
            // When
            notification.markAsFailed("Error 1");
            notification.markAsFailed("Error 2");
            notification.markAsFailed("Error 3");
            
            // Then
            assertEquals(3, notification.getRetryCount());
            assertEquals(NotificationStatus.FAILED, notification.getStatus());
        }
    }
    
    private Notification createTestNotification() {
        EmailAddress recipient = new EmailAddress(VALID_EMAIL);
        return Notification.create(
            NotificationType.WELCOME,
            recipient,
            VALID_SUBJECT,
            null
        );
    }
}

