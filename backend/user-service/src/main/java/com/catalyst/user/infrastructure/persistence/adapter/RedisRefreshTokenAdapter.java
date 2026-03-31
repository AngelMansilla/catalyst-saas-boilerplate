package com.catalyst.user.infrastructure.persistence.adapter;

import com.catalyst.user.application.ports.output.RefreshTokenPort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Redis-backed adapter implementing the RefreshTokenPort output port.
 * Uses StringRedisTemplate (auto-configured by Spring Boot) for simple
 * key → value storage of refresh tokens.
 *
 * <p>Key schema: {@code refresh_token:{tokenValue}} → {@code {userId as UUID string}}
 */
@Component
public class RedisRefreshTokenAdapter implements RefreshTokenPort {

    private static final String KEY_PREFIX = "refresh_token:";

    private final StringRedisTemplate stringRedisTemplate;

    public RedisRefreshTokenAdapter(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public void store(String refreshToken, UUID userId, long ttlSeconds) {
        String key = KEY_PREFIX + refreshToken;
        stringRedisTemplate.opsForValue().set(key, userId.toString(), ttlSeconds, TimeUnit.SECONDS);
    }

    @Override
    public Optional<UUID> findUserIdByToken(String refreshToken) {
        String key = KEY_PREFIX + refreshToken;
        String value = stringRedisTemplate.opsForValue().get(key);
        if (value == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            // Corrupted value — treat as missing and clean up
            stringRedisTemplate.delete(key);
            return Optional.empty();
        }
    }

    @Override
    public void delete(String refreshToken) {
        stringRedisTemplate.delete(KEY_PREFIX + refreshToken);
    }
}
