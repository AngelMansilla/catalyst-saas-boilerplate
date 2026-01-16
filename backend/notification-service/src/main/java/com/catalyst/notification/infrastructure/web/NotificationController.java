package com.catalyst.notification.infrastructure.web;

import com.catalyst.notification.application.dto.NotificationResponse;
import com.catalyst.notification.application.dto.SendNotificationRequest;
import com.catalyst.notification.application.ports.input.GetNotificationUseCase;
import com.catalyst.notification.application.ports.input.SendNotificationUseCase;
import com.catalyst.notification.domain.valueobject.NotificationId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for notification operations.
 * 
 * <p>This controller provides endpoints for:
 * <ul>
 *   <li>Retrieving notification status</li>
 *   <li>Manually sending notifications (for testing/debugging)</li>
 * </ul>
 * 
 * <p>Note: The notification service is primarily event-driven via Kafka.
 * Most notifications are sent automatically when consuming events from user-service
 * and payment-service.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notification management and status queries")
public class NotificationController {
    
    private final GetNotificationUseCase getNotificationUseCase;
    private final SendNotificationUseCase sendNotificationUseCase;
    
    @GetMapping("/{id}")
    @Operation(
        summary = "Get notification by ID",
        description = "Retrieves a notification by its unique identifier"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Notification found",
            content = @Content(schema = @Schema(implementation = NotificationResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Notification not found"
        )
    })
    public ResponseEntity<NotificationResponse> getNotification(
            @Parameter(description = "Notification ID", required = true)
            @PathVariable UUID id) {
        log.debug("Retrieving notification: {}", id);
        
        return getNotificationUseCase.getById(new NotificationId(id))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/send")
    @Operation(
        summary = "Send notification manually",
        description = "Manually sends an email notification. Primarily used for testing and debugging. " +
                     "In production, notifications are typically sent automatically via Kafka events."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Notification sent successfully",
            content = @Content(schema = @Schema(implementation = NotificationResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request data"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Failed to send notification"
        )
    })
    public ResponseEntity<NotificationResponse> sendNotification(
            @Valid @RequestBody SendNotificationRequest request) {
        log.info("Manual notification send request: type={}, recipient={}", 
                request.type(), request.recipientEmail());
        
        NotificationResponse response = sendNotificationUseCase.send(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
