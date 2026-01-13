package com.catalyst.user.infrastructure.web;

import com.catalyst.user.application.dto.*;
import com.catalyst.user.application.ports.input.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for authentication endpoints.
 * These endpoints are public and don't require authentication.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "User registration and authentication")
public class AuthController {
    
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    
    private final RegisterUserUseCase registerUserUseCase;
    private final ValidateCredentialsUseCase validateCredentialsUseCase;
    private final RequestPasswordResetUseCase requestPasswordResetUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;
    
    public AuthController(
            RegisterUserUseCase registerUserUseCase,
            ValidateCredentialsUseCase validateCredentialsUseCase,
            RequestPasswordResetUseCase requestPasswordResetUseCase,
            ResetPasswordUseCase resetPasswordUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.validateCredentialsUseCase = validateCredentialsUseCase;
        this.requestPasswordResetUseCase = requestPasswordResetUseCase;
        this.resetPasswordUseCase = resetPasswordUseCase;
    }
    
    @PostMapping("/register")
    @Operation(summary = "Register a new user", 
               description = "Creates a new user account with email and password")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request for email: {}", request.email());
        UserResponse response = registerUserUseCase.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/validate")
    @Operation(summary = "Validate credentials", 
               description = "Validates email/password credentials for NextAuth")
    public ResponseEntity<UserResponse> validate(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        log.debug("Credential validation request for email: {}", request.email());
        String ipAddress = getClientIpAddress(httpRequest);
        UserResponse response = validateCredentialsUseCase.validate(request, ipAddress);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset",
               description = "Sends a password reset email if the account exists")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody PasswordResetRequest request,
            HttpServletRequest httpRequest) {
        log.info("Password reset request for email: {}", request.email());
        String ipAddress = getClientIpAddress(httpRequest);
        requestPasswordResetUseCase.requestReset(request, ipAddress);
        
        // Always return success to prevent email enumeration
        return ResponseEntity.ok(Map.of(
            "message", "If an account exists with this email, a password reset link will be sent"
        ));
    }
    
    @PostMapping("/reset-password")
    @Operation(summary = "Reset password",
               description = "Resets password using a valid reset token")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody NewPasswordRequest request) {
        log.info("Password reset with token");
        resetPasswordUseCase.resetPassword(request);
        return ResponseEntity.ok(Map.of(
            "message", "Password has been reset successfully"
        ));
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

