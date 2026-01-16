package com.catalyst.notification.application.ports.input;

import com.catalyst.notification.application.dto.NotificationResponse;
import com.catalyst.notification.domain.valueobject.NotificationId;

import java.util.Optional;

/**
 * Use case for retrieving notifications.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
public interface GetNotificationUseCase {
    
    /**
     * Retrieves a notification by ID.
     * 
     * @param id the notification ID
     * @return the notification response if found
     */
    Optional<NotificationResponse> getById(NotificationId id);
}
