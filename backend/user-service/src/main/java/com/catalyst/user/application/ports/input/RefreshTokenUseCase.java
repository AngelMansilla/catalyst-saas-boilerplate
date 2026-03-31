package com.catalyst.user.application.ports.input;

import com.catalyst.shared.domain.auth.TokenResponse;

/**
 * Input port for refreshing an access token using a valid refresh token.
 * Implements token rotation — the presented refresh token is revoked and a new pair is issued.
 */
public interface RefreshTokenUseCase {

    /**
     * Validates a refresh token and issues a new token pair (rotation).
     * The old refresh token is deleted from Redis atomically.
     * A new refresh token is stored with a fresh TTL.
     *
     * @param refreshToken the opaque refresh token string
     * @return TokenResponse with new access token and new refresh token
     * @throws com.catalyst.shared.domain.exception.AuthenticationException if token is invalid or expired
     */
    TokenResponse refresh(String refreshToken);
}
