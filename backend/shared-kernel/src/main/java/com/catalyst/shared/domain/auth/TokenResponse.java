package com.catalyst.shared.domain.auth;

import java.time.Instant;

/**
 * Response DTO for token refresh.
 */
public record TokenResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    long expiresIn,
    Instant expiresAt
) {
    
    private static final String BEARER_TYPE = "Bearer";
    
    /**
     * Creates a token response.
     */
    public static TokenResponse of(
            String accessToken,
            String refreshToken,
            long expiresInSeconds) {
        return new TokenResponse(
            accessToken,
            refreshToken,
            BEARER_TYPE,
            expiresInSeconds,
            Instant.now().plusSeconds(expiresInSeconds)
        );
    }
    
    /**
     * Creates a token response without new refresh token.
     */
    public static TokenResponse accessTokenOnly(
            String accessToken,
            long expiresInSeconds) {
        return new TokenResponse(
            accessToken,
            null,
            BEARER_TYPE,
            expiresInSeconds,
            Instant.now().plusSeconds(expiresInSeconds)
        );
    }
}

