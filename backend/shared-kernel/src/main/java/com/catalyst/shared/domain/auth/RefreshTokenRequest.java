package com.catalyst.shared.domain.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for refreshing access token.
 */
public record RefreshTokenRequest(
    @NotBlank(message = "Refresh token is required")
    String refreshToken
) {
    
    public static RefreshTokenRequest of(String refreshToken) {
        return new RefreshTokenRequest(refreshToken);
    }
}

