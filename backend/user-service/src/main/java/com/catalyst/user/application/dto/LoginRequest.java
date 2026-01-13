package com.catalyst.user.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for credential validation (used by NextAuth).
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
public record LoginRequest(
        
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,
        
        @NotBlank(message = "Password is required")
        String password
) {
}

