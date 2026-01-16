package com.catalyst.notification.application.service;

import com.catalyst.notification.application.dto.NotificationResponse;
import com.catalyst.notification.application.dto.SendNotificationRequest;
import com.catalyst.notification.application.ports.input.GetNotificationUseCase;
import com.catalyst.notification.application.ports.input.SendNotificationUseCase;
import com.catalyst.notification.application.ports.output.EmailSender;
import com.catalyst.notification.application.ports.output.NotificationRepository;
import com.catalyst.notification.application.ports.output.TemplateRenderer;
import com.catalyst.notification.domain.exception.EmailDeliveryException;
import com.catalyst.notification.domain.model.Notification;
import com.catalyst.notification.domain.valueobject.EmailAddress;
import com.catalyst.notification.domain.valueobject.NotificationId;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service implementing notification use cases.
 * 
 * <p>This service orchestrates domain logic, template rendering, email delivery,
 * and persistence.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
@Service
@Transactional
public class NotificationApplicationService implements SendNotificationUseCase, GetNotificationUseCase {
    
    private static final Logger log = LoggerFactory.getLogger(NotificationApplicationService.class);
    
    private final NotificationRepository notificationRepository;
    private final TemplateRenderer templateRenderer;
    private final EmailSender emailSender;
    
    public NotificationApplicationService(
            NotificationRepository notificationRepository,
            TemplateRenderer templateRenderer,
            EmailSender emailSender) {
        this.notificationRepository = notificationRepository;
        this.templateRenderer = templateRenderer;
        this.emailSender = emailSender;
    }
    
    @Override
    public NotificationResponse send(SendNotificationRequest request) {
        log.info("Sending {} notification to {}", request.type().getCode(), request.recipientEmail());
        
        try {
            // Create notification domain object
            EmailAddress recipient = new EmailAddress(request.recipientEmail());
            Notification notification = Notification.create(
                request.type(),
                recipient,
                request.subject(),
                request.templateData()
            );
            
            // Save notification (PENDING status)
            Notification saved = notificationRepository.save(notification);
            
            // Render template (validate template exists)
            templateRenderer.render(request.type(), request.templateData());
            
            // Send email
            try {
                emailSender.send(saved);
                saved.markAsSent();
            } catch (EmailDeliveryException e) {
                log.error("Failed to send notification {}: {}", saved.getId(), e.getMessage());
                saved.markAsFailed(e.getMessage());
                // Save failed notification before throwing exception
                notificationRepository.save(saved);
                throw e;
            }
            
            // Update notification status
            Notification updated = notificationRepository.save(saved);
            
            log.info("Notification {} sent successfully to {}", updated.getId(), request.recipientEmail());
            
            return NotificationResponse.fromDomain(updated);
            
        } catch (Exception e) {
            log.error("Error processing notification: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<NotificationResponse> getById(NotificationId id) {
        log.debug("Retrieving notification by ID: {}", id);
        return notificationRepository.findById(id)
            .map(NotificationResponse::fromDomain);
    }
}

