package com.catalyst.user.application.dto;

import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating user profile.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
public record UpdateProfileRequest(
        
        @Size(max = 255, message = "Name cannot exceed 255 characters")
        String name,
        
        @Size(max = 2048, message = "Image URL cannot exceed 2048 characters")
        String imageUrl
) {
}

