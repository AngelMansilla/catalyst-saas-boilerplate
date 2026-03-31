package com.catalyst.user.application.ports.output;

import java.util.Optional;
import java.util.UUID;

/**
 * Output port for refresh token persistence.
 * Implemented by the Redis adapter in the infrastructure layer.
 */
public interface RefreshTokenPort {

    /**
     * Stores a refresh token → userId mapping with the given TTL.
     * Overwrites any existing entry for the same token value.
     *
     * @param refreshToken the opaque refresh token string (Redis key suffix)
     * @param userId       the user's UUID (stored as string value)
     * @param ttlSeconds   TTL in seconds for this entry
     */
    void store(String refreshToken, UUID userId, long ttlSeconds);

    /**
     * Looks up the userId associated with a refresh token.
     *
     * @param refreshToken the opaque refresh token string
     * @return Optional userId if found and not expired; empty if not found or expired
     */
    Optional<UUID> findUserIdByToken(String refreshToken);

    /**
     * Deletes a refresh token from the store.
     * Idempotent — no-op if the key does not exist.
     *
     * @param refreshToken the opaque refresh token string
     */
    void delete(String refreshToken);
}
