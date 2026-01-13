package com.catalyst.user.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for syncing OAuth users from NextAuth.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
public record SyncUserRequest(
        
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,
        
        String name,
        
        String imageUrl,
        
        @NotBlank(message = "Provider is required")
        String provider,
        
        @NotBlank(message = "Provider account ID is required")
        String providerAccountId
) {
}

