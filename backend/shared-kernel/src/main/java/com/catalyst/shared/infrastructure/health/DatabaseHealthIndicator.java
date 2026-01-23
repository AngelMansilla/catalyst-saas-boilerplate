package com.catalyst.shared.infrastructure.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Custom health indicator for PostgreSQL database connectivity.
 * Checks if the service can establish a connection to the database.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(5)) {
                log.debug("Database health check: UP");
                return Health.up()
                        .withDetail("database", "PostgreSQL")
                        .withDetail("status", "Connection successful")
                        .build();
            } else {
                log.warn("Database health check: Connection validation failed");
                return Health.down()
                        .withDetail("database", "PostgreSQL")
                        .withDetail("status", "Connection validation failed")
                        .build();
            }
        } catch (SQLException e) {
            log.error("Database health check failed: {}", e.getMessage(), e);
            return Health.down()
                    .withDetail("database", "PostgreSQL")
                    .withDetail("error", e.getMessage())
                    .withException(e)
                    .build();
        }
    }
}
