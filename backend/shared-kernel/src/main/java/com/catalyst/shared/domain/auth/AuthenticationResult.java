package com.catalyst.shared.domain.auth;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Represents the result of an authentication attempt.
 * Domain layer - no framework dependencies.
 */
public record AuthenticationResult(
    boolean authenticated,
    UUID userId,
    String accessToken,
    String refreshToken,
    Instant accessTokenExpiresAt,
    Instant refreshTokenExpiresAt,
    String errorCode,
    String errorMessage
) {
    
    /**
     * Creates a successful authentication result.
     */
    public static AuthenticationResult success(
            UUID userId,
            String accessToken,
            String refreshToken,
            Instant accessTokenExpiresAt,
            Instant refreshTokenExpiresAt) {
        return new AuthenticationResult(
            true,
            userId,
            accessToken,
            refreshToken,
            accessTokenExpiresAt,
            refreshTokenExpiresAt,
            null,
            null
        );
    }
    
    /**
     * Creates a failed authentication result.
     */
    public static AuthenticationResult failure(String errorCode, String errorMessage) {
        return new AuthenticationResult(
            false,
            null,
            null,
            null,
            null,
            null,
            errorCode,
            errorMessage
        );
    }
    
    /**
     * Returns the user ID if authentication was successful.
     */
    public Optional<UUID> getUserId() {
        return Optional.ofNullable(userId);
    }
    
    /**
     * Returns the access token if authentication was successful.
     */
    public Optional<String> getAccessToken() {
        return Optional.ofNullable(accessToken);
    }
    
    /**
     * Returns the refresh token if authentication was successful.
     */
    public Optional<String> getRefreshToken() {
        return Optional.ofNullable(refreshToken);
    }
}

