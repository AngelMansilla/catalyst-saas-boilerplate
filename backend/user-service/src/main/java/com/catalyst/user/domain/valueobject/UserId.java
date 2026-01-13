package com.catalyst.user.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

/**
 * Value object representing a unique user identifier.
 * 
 * <p>This immutable value object wraps a UUID to provide type safety
 * and prevent confusion with other UUID-based identifiers.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
public final class UserId {
    
    private final UUID value;
    
    private UserId(UUID value) {
        this.value = value;
    }
    
    /**
     * Creates a new random UserId.
     * 
     * @return a new UserId with a random UUID
     */
    public static UserId generate() {
        return new UserId(UUID.randomUUID());
    }
    
    /**
     * Creates a UserId from an existing UUID.
     * 
     * @param uuid the UUID value
     * @return a UserId instance
     * @throws IllegalArgumentException if uuid is null
     */
    public static UserId of(UUID uuid) {
        if (uuid == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        return new UserId(uuid);
    }
    
    /**
     * Creates a UserId from a string representation.
     * 
     * @param value the UUID string
     * @return a UserId instance
     * @throws IllegalArgumentException if the string is not a valid UUID
     */
    public static UserId fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("User ID string cannot be null or blank");
        }
        try {
            return new UserId(UUID.fromString(value.trim()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid user ID format: " + value, e);
        }
    }
    
    /**
     * Returns the UUID value.
     * 
     * @return the underlying UUID
     */
    public UUID getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserId userId = (UserId) o;
        return Objects.equals(value, userId.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return value.toString();
    }
}

