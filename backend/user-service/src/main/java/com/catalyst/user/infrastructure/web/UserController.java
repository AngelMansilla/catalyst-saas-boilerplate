package com.catalyst.user.infrastructure.web;

import com.catalyst.user.application.dto.*;
import com.catalyst.user.application.ports.input.*;
import com.catalyst.user.domain.valueobject.UserId;
import com.catalyst.shared.infrastructure.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for user management endpoints.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "User management endpoints")
public class UserController {
    
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    
    private final SyncSocialUserUseCase syncSocialUserUseCase;
    private final GetUserProfileUseCase getUserProfileUseCase;
    private final UpdateUserProfileUseCase updateUserProfileUseCase;
    
    @Value("${user.internal-api-key}")
    private String internalApiKey;
    
    public UserController(
            SyncSocialUserUseCase syncSocialUserUseCase,
            GetUserProfileUseCase getUserProfileUseCase,
            UpdateUserProfileUseCase updateUserProfileUseCase) {
        this.syncSocialUserUseCase = syncSocialUserUseCase;
        this.getUserProfileUseCase = getUserProfileUseCase;
        this.updateUserProfileUseCase = updateUserProfileUseCase;
    }
    
    @PostMapping("/sync")
    @Operation(summary = "Sync OAuth user",
               description = "Syncs user from OAuth provider (called by NextAuth)")
    public ResponseEntity<UserResponse> syncUser(
            @Valid @RequestBody SyncUserRequest request,
            @RequestHeader("X-API-Key") String apiKey) {
        
        // Validate internal API key
        if (!internalApiKey.equals(apiKey)) {
            log.warn("Invalid API key for user sync");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        log.info("Syncing OAuth user: {} from {}", request.email(), request.provider());
        UserResponse response = syncSocialUserUseCase.sync(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/me")
    @Operation(summary = "Get current user profile",
               description = "Returns the authenticated user's profile")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserResponse> getCurrentUser(
            @AuthenticationPrincipal UserPrincipal principal) {
        
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        log.debug("Getting profile for user: {}", principal.id());
        UserId userId = UserId.of(principal.id());
        UserResponse response = getUserProfileUseCase.getById(userId);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/me")
    @Operation(summary = "Update current user profile",
               description = "Updates the authenticated user's profile")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserResponse> updateCurrentUser(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateProfileRequest request) {
        
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        log.info("Updating profile for user: {}", principal.id());
        UserId userId = UserId.of(principal.id());
        UserResponse response = updateUserProfileUseCase.updateProfile(userId, request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID",
               description = "Returns a user's profile by ID (admin only)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        log.debug("Admin getting user by ID: {}", id);
        UserId userId = UserId.of(id);
        UserResponse response = getUserProfileUseCase.getById(userId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @Operation(summary = "List all users",
               description = "Returns paginated list of all users (admin only)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.debug("Admin listing users: page={}, size={}", page, size);
        List<UserResponse> users = getUserProfileUseCase.listAll(page, size);
        return ResponseEntity.ok(users);
    }
}

