package com.catalyst.shared.domain.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for user login.
 */
public record LoginRequest(
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email,
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    String password,
    
    boolean rememberMe
) {
    
    /**
     * Creates a login request with default rememberMe = false.
     */
    public static LoginRequest of(String email, String password) {
        return new LoginRequest(email, password, false);
    }
}

