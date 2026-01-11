package com.catalyst.shared.domain.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for user registration.
 */
public record RegisterRequest(
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    String name,
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email,
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
        message = "Password must contain at least one uppercase letter, one lowercase letter, and one digit"
    )
    String password,
    
    @NotBlank(message = "Password confirmation is required")
    String confirmPassword,
    
    boolean acceptTerms
) {
    
    /**
     * Validates that password and confirmation match.
     */
    public boolean passwordsMatch() {
        return password != null && password.equals(confirmPassword);
    }
    
    /**
     * Creates a register request with terms accepted.
     */
    public static RegisterRequest of(String name, String email, String password) {
        return new RegisterRequest(name, email, password, password, true);
    }
}

