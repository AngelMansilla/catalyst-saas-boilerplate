package com.catalyst.notification.infrastructure.kafka;

import com.catalyst.notification.application.dto.SendNotificationRequest;
import com.catalyst.notification.application.ports.input.SendNotificationUseCase;
import com.catalyst.notification.domain.valueobject.NotificationType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka consumer for user-service events.
 * Handles user registration and password reset events.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
@Component
public class UserEventConsumer {
    
    private static final Logger log = LoggerFactory.getLogger(UserEventConsumer.class);
    
    private final SendNotificationUseCase sendNotificationUseCase;
    private final ObjectMapper objectMapper;
    
    @Value("${email.base-url:http://localhost:3000}")
    private String baseUrl;
    
    public UserEventConsumer(SendNotificationUseCase sendNotificationUseCase) {
        this.sendNotificationUseCase = sendNotificationUseCase;
        this.objectMapper = new ObjectMapper();
    }
    
    @RetryableTopic(
        attempts = "4",
        dltTopicSuffix = ".dlq",
        autoCreateTopics = "true",
        topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE
    )
    @KafkaListener(topics = "${kafka.topics.user-registered:user.registered}")
    public void handleUserRegistered(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        
        log.info("Received UserRegistered event from topic: {}", topic);
        
        try {
            JsonNode envelope = objectMapper.readTree(message);
            JsonNode payload = envelope.get("payload");
            
            String email = payload.get("email").asText();
            String name = payload.get("name").asText();
            
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("userName", name);
            templateData.put("dashboardUrl", baseUrl + "/dashboard");
            
            SendNotificationRequest request = new SendNotificationRequest(
                NotificationType.WELCOME,
                email,
                "Welcome to Catalyst!",
                templateData
            );
            
            sendNotificationUseCase.send(request);
            
            log.info("Welcome email sent to {}", email);
            
        } catch (Exception e) {
            log.error("Failed to process UserRegistered event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process UserRegistered event", e);
        }
    }
    
    @RetryableTopic(
        attempts = "4",
        dltTopicSuffix = ".dlq",
        autoCreateTopics = "true",
        topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE
    )
    @KafkaListener(topics = "${kafka.topics.password-reset-requested:user.password-reset-requested}")
    public void handlePasswordResetRequested(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        
        log.info("Received PasswordResetRequested event from topic: {}", topic);
        
        try {
            JsonNode envelope = objectMapper.readTree(message);
            JsonNode payload = envelope.get("payload");
            
            String email = payload.get("email").asText();
            String token = payload.get("token").asText();
            
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("userName", email.split("@")[0]); // Extract name from email
            templateData.put("resetUrl", baseUrl + "/reset-password?token=" + token);
            templateData.put("expiryHours", "24");
            
            SendNotificationRequest request = new SendNotificationRequest(
                NotificationType.PASSWORD_RESET,
                email,
                "Reset Your Password",
                templateData
            );
            
            sendNotificationUseCase.send(request);
            
            log.info("Password reset email sent to {}", email);
            
        } catch (Exception e) {
            log.error("Failed to process PasswordResetRequested event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process PasswordResetRequested event", e);
        }
    }
    
    @RetryableTopic(
        attempts = "4",
        dltTopicSuffix = ".dlq",
        autoCreateTopics = "true",
        topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE
    )
    @KafkaListener(topics = "${kafka.topics.password-reset-completed:user.password-reset-completed}")
    public void handlePasswordResetCompleted(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        
        log.info("Received PasswordResetCompleted event from topic: {}", topic);
        
        try {
            JsonNode envelope = objectMapper.readTree(message);
            JsonNode payload = envelope.get("payload");
            
            String email = payload.get("email").asText();
            
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("userName", email.split("@")[0]);
            templateData.put("dashboardUrl", baseUrl + "/dashboard");
            
            SendNotificationRequest request = new SendNotificationRequest(
                NotificationType.PASSWORD_RESET_CONFIRMATION,
                email,
                "Password Changed Successfully",
                templateData
            );
            
            sendNotificationUseCase.send(request);
            
            log.info("Password reset confirmation email sent to {}", email);
            
        } catch (Exception e) {
            log.error("Failed to process PasswordResetCompleted event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process PasswordResetCompleted event", e);
        }
    }
    
    @DltHandler
    public void handleDlt(String message, @Header(KafkaHeaders.ORIGINAL_TOPIC) String originalTopic) {
        log.error("Message sent to DLT for topic {}: {}", originalTopic, message);
        // In production, you might want to:
        // 1. Log to a monitoring system
        // 2. Send an alert
        // 3. Store in a database for manual review
    }
}

