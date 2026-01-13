package com.catalyst.user.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for requesting a password reset.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
public record PasswordResetRequest(
        
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email
) {
}

