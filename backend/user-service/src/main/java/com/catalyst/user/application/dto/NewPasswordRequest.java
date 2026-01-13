package com.catalyst.user.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for setting a new password with reset token.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
public record NewPasswordRequest(
        
        @NotBlank(message = "Token is required")
        String token,
        
        @NotBlank(message = "New password is required")
        @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
        String newPassword
) {
}

