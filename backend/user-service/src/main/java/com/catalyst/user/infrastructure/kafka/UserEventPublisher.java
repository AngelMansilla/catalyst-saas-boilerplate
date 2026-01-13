package com.catalyst.user.infrastructure.kafka;

import com.catalyst.user.application.ports.output.EventPublisher;
import com.catalyst.user.domain.event.PasswordResetCompleted;
import com.catalyst.user.domain.event.PasswordResetRequested;
import com.catalyst.user.domain.event.UserLoggedIn;
import com.catalyst.user.domain.event.UserRegistered;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Kafka implementation of the EventPublisher port.
 * Publishes user domain events to Kafka topics.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
@Component
public class UserEventPublisher implements EventPublisher {
    
    private static final Logger log = LoggerFactory.getLogger(UserEventPublisher.class);
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${kafka.topics.user-registered:user.registered}")
    private String userRegisteredTopic;
    
    @Value("${kafka.topics.user-logged-in:user.logged-in}")
    private String userLoggedInTopic;
    
    @Value("${kafka.topics.password-reset-requested:user.password-reset-requested}")
    private String passwordResetRequestedTopic;
    
    @Value("${kafka.topics.password-reset-completed:user.password-reset-completed}")
    private String passwordResetCompletedTopic;
    
    public UserEventPublisher(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    
    @Override
    public void publish(UserRegistered event) {
        log.info("Publishing UserRegistered event for user: {}", event.userId());
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", event.userId().getValue().toString());
        payload.put("email", event.email().getValue());
        payload.put("name", event.name());
        payload.put("provider", event.provider().getCode());
        payload.put("role", event.role().getCode());
        
        publishEvent(userRegisteredTopic, event.userId().toString(), event.eventId(), 
                "UserRegistered", payload);
    }
    
    @Override
    public void publish(UserLoggedIn event) {
        log.debug("Publishing UserLoggedIn event for user: {}", event.userId());
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", event.userId().getValue().toString());
        payload.put("email", event.email().getValue());
        payload.put("provider", event.provider().getCode());
        payload.put("ipAddress", event.ipAddress());
        
        publishEvent(userLoggedInTopic, event.userId().toString(), event.eventId(),
                "UserLoggedIn", payload);
    }
    
    @Override
    public void publish(PasswordResetRequested event) {
        log.info("Publishing PasswordResetRequested event for user: {}", event.userId());
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", event.userId().getValue().toString());
        payload.put("email", event.email().getValue());
        payload.put("token", event.token());
        payload.put("expiresAt", event.expiresAt().toString());
        
        publishEvent(passwordResetRequestedTopic, event.userId().toString(), event.eventId(),
                "PasswordResetRequested", payload);
    }
    
    @Override
    public void publish(PasswordResetCompleted event) {
        log.info("Publishing PasswordResetCompleted event for user: {}", event.userId());
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", event.userId().getValue().toString());
        payload.put("email", event.email().getValue());
        
        publishEvent(passwordResetCompletedTopic, event.userId().toString(), event.eventId(),
                "PasswordResetCompleted", payload);
    }
    
    private void publishEvent(String topic, String key, UUID eventId, String eventType, 
            Map<String, Object> payload) {
        try {
            Map<String, Object> envelope = createEnvelope(eventId, eventType, payload);
            String json = objectMapper.writeValueAsString(envelope);
            
            kafkaTemplate.send(topic, key, json)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to publish event {} to topic {}: {}", 
                                    eventType, topic, ex.getMessage());
                        } else {
                            log.debug("Event {} published to topic {} at offset {}", 
                                    eventType, topic, result.getRecordMetadata().offset());
                        }
                    });
                    
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event {}: {}", eventType, e.getMessage());
        }
    }
    
    private Map<String, Object> createEnvelope(UUID eventId, String eventType, 
            Map<String, Object> payload) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("eventId", eventId.toString());
        metadata.put("eventType", eventType);
        metadata.put("version", "1.0");
        metadata.put("timestamp", Instant.now().toString());
        metadata.put("source", "user-service");
        
        Map<String, Object> envelope = new HashMap<>();
        envelope.put("metadata", metadata);
        envelope.put("payload", payload);
        
        return envelope;
    }
}

