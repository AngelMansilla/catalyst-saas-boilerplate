package com.catalyst.shared.infrastructure.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

/**
 * Custom health indicator for Redis connectivity.
 * Checks if the service can connect to Redis and perform a PING operation.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisHealthIndicator implements HealthIndicator {

    private final RedisConnectionFactory redisConnectionFactory;

    @Override
    public Health health() {
        try (RedisConnection connection = redisConnectionFactory.getConnection()) {
            String pong = connection.ping();
            if ("PONG".equals(pong)) {
                log.debug("Redis health check: UP");
                return Health.up()
                        .withDetail("redis", "Connected")
                        .withDetail("status", "PING successful")
                        .build();
            } else {
                log.warn("Redis health check: Unexpected PING response: {}", pong);
                return Health.down()
                        .withDetail("redis", "Unexpected response")
                        .withDetail("status", "PING returned: " + pong)
                        .build();
            }
        } catch (Exception e) {
            log.error("Redis health check failed: {}", e.getMessage(), e);
            return Health.down()
                    .withDetail("redis", "Connection failed")
                    .withDetail("error", e.getMessage())
                    .withException(e)
                    .build();
        }
    }
}
