package com.catalyst.shared.domain.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating password with reset token.
 */
public record PasswordUpdateRequest(
    @NotBlank(message = "Token is required")
    String token,
    
    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
        message = "Password must contain at least one uppercase letter, one lowercase letter, and one digit"
    )
    String newPassword,
    
    @NotBlank(message = "Password confirmation is required")
    String confirmPassword
) {
    
    /**
     * Validates that password and confirmation match.
     */
    public boolean passwordsMatch() {
        return newPassword != null && newPassword.equals(confirmPassword);
    }
    
    public static PasswordUpdateRequest of(String token, String newPassword) {
        return new PasswordUpdateRequest(token, newPassword, newPassword);
    }
}

