package com.catalyst.notification.domain.model;

import com.catalyst.notification.domain.valueobject.EmailAddress;
import com.catalyst.notification.domain.valueobject.NotificationId;
import com.catalyst.notification.domain.valueobject.NotificationStatus;
import com.catalyst.notification.domain.valueobject.NotificationType;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Notification aggregate root representing an email notification.
 * 
 * <p>This entity encapsulates:
 * <ul>
 *   <li>Notification identity and type</li>
 *   <li>Recipient information</li>
 *   <li>Template data (variables for Thymeleaf)</li>
 *   <li>Delivery status and tracking</li>
 *   <li>Retry information</li>
 * </ul>
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
public class Notification {
    
    private NotificationId id;
    private NotificationType type;
    private EmailAddress recipient;
    private String subject;
    private Map<String, Object> templateData;
    private NotificationStatus status;
    private String errorMessage;
    private int retryCount;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private LocalDateTime updatedAt;
    
    // Private constructor for factory methods
    private Notification() {
        this.templateData = new HashMap<>();
    }
    
    // ========================
    // Factory Methods
    // ========================
    
    /**
     * Creates a new notification for email delivery.
     * 
     * @param type the notification type
     * @param recipient the recipient email address
     * @param subject the email subject
     * @param templateData the template variables
     * @return a new Notification instance
     */
    public static Notification create(
            NotificationType type,
            EmailAddress recipient,
            String subject,
            Map<String, Object> templateData) {
        
        Notification notification = new Notification();
        notification.id = NotificationId.generate();
        notification.type = Objects.requireNonNull(type, "Notification type is required");
        notification.recipient = Objects.requireNonNull(recipient, "Recipient is required");
        notification.subject = Objects.requireNonNull(subject, "Subject is required");
        if (templateData != null) {
            notification.templateData.putAll(templateData);
        }
        notification.status = NotificationStatus.PENDING;
        notification.retryCount = 0;
        notification.createdAt = LocalDateTime.now();
        notification.updatedAt = notification.createdAt;
        
        return notification;
    }
    
    // ========================
    // Business Logic
    // ========================
    
    /**
     * Marks the notification as successfully sent.
     */
    public void markAsSent() {
        this.status = NotificationStatus.SENT;
        this.sentAt = LocalDateTime.now();
        this.updatedAt = this.sentAt;
        this.errorMessage = null;
    }
    
    /**
     * Marks the notification as failed and increments retry count.
     * 
     * @param errorMessage the error message
     */
    public void markAsFailed(String errorMessage) {
        this.status = NotificationStatus.FAILED;
        this.errorMessage = errorMessage;
        this.retryCount++;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Marks the notification as retrying.
     */
    public void markAsRetrying() {
        this.status = NotificationStatus.RETRYING;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Checks if the notification can be retried.
     * 
     * @param maxRetries the maximum number of retries allowed
     * @return true if retry is allowed
     */
    public boolean canRetry(int maxRetries) {
        return status == NotificationStatus.FAILED 
            && retryCount < maxRetries;
    }
    
    // ========================
    // Getters
    // ========================
    
    public NotificationId getId() {
        return id;
    }
    
    public NotificationType getType() {
        return type;
    }
    
    public EmailAddress getRecipient() {
        return recipient;
    }
    
    public String getSubject() {
        return subject;
    }
    
    public Map<String, Object> getTemplateData() {
        return new HashMap<>(templateData);
    }
    
    public NotificationStatus getStatus() {
        return status;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public int getRetryCount() {
        return retryCount;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getSentAt() {
        return sentAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}

