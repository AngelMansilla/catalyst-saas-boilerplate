package com.catalyst.notification.application.service;

import com.catalyst.notification.application.dto.NotificationResponse;
import com.catalyst.notification.application.dto.SendNotificationRequest;
import com.catalyst.notification.application.ports.output.EmailSender;
import com.catalyst.notification.application.ports.output.NotificationRepository;
import com.catalyst.notification.application.ports.output.TemplateRenderer;
import com.catalyst.notification.domain.exception.EmailDeliveryException;
import com.catalyst.notification.domain.exception.TemplateNotFoundException;
import com.catalyst.notification.domain.model.Notification;
import com.catalyst.notification.domain.valueobject.EmailAddress;
import com.catalyst.notification.domain.valueobject.NotificationStatus;
import com.catalyst.notification.domain.valueobject.NotificationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for NotificationApplicationService.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationApplicationService")
class NotificationApplicationServiceTest {
    
    @Mock
    private NotificationRepository notificationRepository;
    
    @Mock
    private TemplateRenderer templateRenderer;
    
    @Mock
    private EmailSender emailSender;
    
    @InjectMocks
    private NotificationApplicationService notificationApplicationService;
    
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_SUBJECT = "Test Subject";
    
    @Nested
    @DisplayName("Send Notification")
    class SendNotification {
        
        @Test
        @DisplayName("Should send notification successfully")
        void shouldSendNotificationSuccessfully() {
            // Given
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("userName", "John Doe");
            
            SendNotificationRequest request = new SendNotificationRequest(
                NotificationType.WELCOME,
                TEST_EMAIL,
                TEST_SUBJECT,
                templateData
            );
            
            Notification savedNotification = Notification.create(
                NotificationType.WELCOME,
                new EmailAddress(TEST_EMAIL),
                TEST_SUBJECT,
                templateData
            );
            
            Notification sentNotification = Notification.create(
                NotificationType.WELCOME,
                new EmailAddress(TEST_EMAIL),
                TEST_SUBJECT,
                templateData
            );
            sentNotification.markAsSent();
            
            when(notificationRepository.save(any(Notification.class)))
                .thenReturn(savedNotification)
                .thenReturn(sentNotification);
            when(templateRenderer.render(eq(NotificationType.WELCOME), eq(templateData)))
                .thenReturn("<html>Rendered template</html>");
            doNothing().when(emailSender).send(any(Notification.class));
            
            // When
            NotificationResponse response = notificationApplicationService.send(request);
            
            // Then
            assertNotNull(response);
            assertEquals(NotificationType.WELCOME.getCode(), response.type());
            assertEquals(TEST_EMAIL, response.recipientEmail());
            assertEquals(TEST_SUBJECT, response.subject());
            assertEquals(NotificationStatus.SENT, response.status());
            assertNotNull(response.sentAt());
            
            verify(notificationRepository, times(2)).save(any(Notification.class));
            verify(templateRenderer, times(1)).render(eq(NotificationType.WELCOME), eq(templateData));
            verify(emailSender, times(1)).send(any(Notification.class));
        }
        
        @Test
        @DisplayName("Should throw exception when template not found")
        void shouldThrowExceptionWhenTemplateNotFound() {
            // Given
            Map<String, Object> templateData = new HashMap<>();
            SendNotificationRequest request = new SendNotificationRequest(
                NotificationType.WELCOME,
                TEST_EMAIL,
                TEST_SUBJECT,
                templateData
            );
            
            Notification savedNotification = Notification.create(
                NotificationType.WELCOME,
                new EmailAddress(TEST_EMAIL),
                TEST_SUBJECT,
                templateData
            );
            
            when(notificationRepository.save(any(Notification.class)))
                .thenReturn(savedNotification);
            when(templateRenderer.render(eq(NotificationType.WELCOME), eq(templateData)))
                .thenThrow(new TemplateNotFoundException("welcome"));
            
            // When & Then
            assertThrows(TemplateNotFoundException.class, () -> {
                notificationApplicationService.send(request);
            });
            
            verify(notificationRepository, times(1)).save(any(Notification.class));
            verify(templateRenderer, times(1)).render(eq(NotificationType.WELCOME), eq(templateData));
            verify(emailSender, never()).send(any(Notification.class));
        }
        
        @Test
        @DisplayName("Should mark notification as failed when email delivery fails")
        void shouldMarkNotificationAsFailedWhenEmailDeliveryFails() {
            // Given
            Map<String, Object> templateData = new HashMap<>();
            SendNotificationRequest request = new SendNotificationRequest(
                NotificationType.WELCOME,
                TEST_EMAIL,
                TEST_SUBJECT,
                templateData
            );
            
            Notification savedNotification = Notification.create(
                NotificationType.WELCOME,
                new EmailAddress(TEST_EMAIL),
                TEST_SUBJECT,
                templateData
            );
            
            Notification failedNotification = Notification.create(
                NotificationType.WELCOME,
                new EmailAddress(TEST_EMAIL),
                TEST_SUBJECT,
                templateData
            );
            failedNotification.markAsFailed("SMTP connection failed");
            
            when(notificationRepository.save(any(Notification.class)))
                .thenReturn(savedNotification)
                .thenReturn(failedNotification);
            when(templateRenderer.render(eq(NotificationType.WELCOME), eq(templateData)))
                .thenReturn("<html>Rendered template</html>");
            doThrow(new EmailDeliveryException("SMTP connection failed"))
                .when(emailSender).send(any(Notification.class));
            
            // When & Then
            assertThrows(EmailDeliveryException.class, () -> {
                notificationApplicationService.send(request);
            });
            
            verify(notificationRepository, times(2)).save(any(Notification.class));
            verify(templateRenderer, times(1)).render(eq(NotificationType.WELCOME), eq(templateData));
            verify(emailSender, times(1)).send(any(Notification.class));
        }
        
        @Test
        @DisplayName("Should handle notification with null template data")
        void shouldHandleNotificationWithNullTemplateData() {
            // Given
            SendNotificationRequest request = new SendNotificationRequest(
                NotificationType.PASSWORD_RESET,
                TEST_EMAIL,
                TEST_SUBJECT,
                null
            );
            
            Notification savedNotification = Notification.create(
                NotificationType.PASSWORD_RESET,
                new EmailAddress(TEST_EMAIL),
                TEST_SUBJECT,
                null
            );
            
            Notification sentNotification = Notification.create(
                NotificationType.PASSWORD_RESET,
                new EmailAddress(TEST_EMAIL),
                TEST_SUBJECT,
                null
            );
            sentNotification.markAsSent();
            
            when(notificationRepository.save(any(Notification.class)))
                .thenReturn(savedNotification)
                .thenReturn(sentNotification);
            when(templateRenderer.render(eq(NotificationType.PASSWORD_RESET), any()))
                .thenReturn("<html>Rendered template</html>");
            doNothing().when(emailSender).send(any(Notification.class));
            
            // When
            NotificationResponse response = notificationApplicationService.send(request);
            
            // Then
            assertNotNull(response);
            assertEquals(NotificationStatus.SENT, response.status());
            
            verify(notificationRepository, times(2)).save(any(Notification.class));
            verify(templateRenderer, times(1)).render(eq(NotificationType.PASSWORD_RESET), any());
            verify(emailSender, times(1)).send(any(Notification.class));
        }
    }
}

