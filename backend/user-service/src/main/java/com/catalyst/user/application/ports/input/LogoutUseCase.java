package com.catalyst.user.application.ports.input;

/**
 * Input port for invalidating a refresh token server-side (logout).
 * The operation is idempotent — deleting a non-existent token is not an error.
 */
public interface LogoutUseCase {

    /**
     * Invalidates a refresh token by deleting it from Redis.
     * Idempotent — deleting a non-existent token succeeds silently.
     *
     * @param refreshToken the opaque refresh token string to invalidate
     */
    void logout(String refreshToken);
}
