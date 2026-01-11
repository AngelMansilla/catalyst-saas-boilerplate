package com.catalyst.shared.domain.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for password reset.
 */
public record PasswordResetRequest(
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email
) {
    
    public static PasswordResetRequest of(String email) {
        return new PasswordResetRequest(email);
    }
}

