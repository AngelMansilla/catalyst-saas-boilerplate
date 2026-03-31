package com.catalyst.user.infrastructure.web;

import com.catalyst.shared.domain.auth.LoginResponse;
import com.catalyst.shared.domain.auth.RefreshTokenRequest;
import com.catalyst.shared.domain.auth.TokenResponse;
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
    private final LoginUseCase loginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;

    public AuthController(
            RegisterUserUseCase registerUserUseCase,
            ValidateCredentialsUseCase validateCredentialsUseCase,
            RequestPasswordResetUseCase requestPasswordResetUseCase,
            ResetPasswordUseCase resetPasswordUseCase,
            LoginUseCase loginUseCase,
            RefreshTokenUseCase refreshTokenUseCase,
            LogoutUseCase logoutUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.validateCredentialsUseCase = validateCredentialsUseCase;
        this.requestPasswordResetUseCase = requestPasswordResetUseCase;
        this.resetPasswordUseCase = resetPasswordUseCase;
        this.loginUseCase = loginUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.logoutUseCase = logoutUseCase;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account with email and password")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request for email: {}", request.email());
        UserResponse response = registerUserUseCase.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/validate")
    @Operation(summary = "Validate credentials", description = "Validates email/password credentials for frontend authentication")
    public ResponseEntity<UserResponse> validate(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        log.debug("Credential validation request for email: {}", request.email());
        String ipAddress = getClientIpAddress(httpRequest);
        UserResponse response = validateCredentialsUseCase.validate(request, ipAddress);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset", description = "Sends a password reset email if the account exists")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody PasswordResetRequest request,
            HttpServletRequest httpRequest) {
        log.info("Password reset request for email: {}", request.email());
        String ipAddress = getClientIpAddress(httpRequest);
        requestPasswordResetUseCase.requestReset(request, ipAddress);

        // Always return success to prevent email enumeration
        return ResponseEntity.ok(Map.of(
                "message", "If an account exists with this email, a password reset link will be sent"));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Resets password using a valid reset token")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody NewPasswordRequest request) {
        log.info("Password reset with token");
        resetPasswordUseCase.resetPassword(request);
        return ResponseEntity.ok(Map.of(
                "message", "Password has been reset successfully"));
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticates user with email and password, returns a JWT token pair")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody com.catalyst.shared.domain.auth.LoginRequest request,
            HttpServletRequest httpRequest) {
        log.info("Login request for email: {}", request.email());
        String ipAddress = getClientIpAddress(httpRequest);
        LoginResponse response = loginUseCase.login(request, ipAddress);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh tokens", description = "Issues a new token pair using a valid refresh token")
    public ResponseEntity<TokenResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {
        log.debug("Token refresh request");
        TokenResponse response = refreshTokenUseCase.refresh(request.refreshToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Invalidates the refresh token server-side")
    public ResponseEntity<Void> logout(
            @Valid @RequestBody RefreshTokenRequest request) {
        log.info("Logout request");
        logoutUseCase.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
