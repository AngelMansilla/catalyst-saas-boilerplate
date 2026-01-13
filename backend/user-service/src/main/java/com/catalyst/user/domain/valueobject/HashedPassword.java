package com.catalyst.user.domain.valueobject;

import java.util.Objects;

/**
 * Value object representing a hashed password.
 * 
 * <p>This immutable value object encapsulates a BCrypt-hashed password.
 * It never stores or exposes the plain text password.
 * 
 * <p>Important: The actual hashing is done by the infrastructure layer
 * (PasswordEncoder). This class only holds the already-hashed value.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
public final class HashedPassword {
    
    private static final String BCRYPT_PREFIX = "$2";
    private static final int MIN_HASH_LENGTH = 59;
    
    private final String hash;
    
    private HashedPassword(String hash) {
        this.hash = hash;
    }
    
    /**
     * Creates a HashedPassword from an already-hashed value.
     * 
     * <p>This should only be used with values that were previously
     * hashed by BCrypt. Use the infrastructure layer's PasswordEncoder
     * to create hashes from plain text passwords.
     * 
     * @param hash the BCrypt hash
     * @return a HashedPassword instance
     * @throws IllegalArgumentException if the hash is invalid
     */
    public static HashedPassword fromHash(String hash) {
        if (hash == null || hash.isBlank()) {
            throw new IllegalArgumentException("Password hash cannot be null or blank");
        }
        
        if (!hash.startsWith(BCRYPT_PREFIX) || hash.length() < MIN_HASH_LENGTH) {
            throw new IllegalArgumentException("Invalid BCrypt hash format");
        }
        
        return new HashedPassword(hash);
    }
    
    /**
     * Creates a HashedPassword from a hash, without strict validation.
     * 
     * <p>This is useful when loading from database where we trust the stored value.
     * 
     * @param hash the password hash
     * @return a HashedPassword instance, or null if hash is null/blank
     */
    public static HashedPassword fromTrustedHash(String hash) {
        if (hash == null || hash.isBlank()) {
            return null;
        }
        return new HashedPassword(hash);
    }
    
    /**
     * Returns the hashed password value.
     * 
     * @return the BCrypt hash
     */
    public String getValue() {
        return hash;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HashedPassword that = (HashedPassword) o;
        return Objects.equals(hash, that.hash);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(hash);
    }
    
    /**
     * Returns a masked string for logging purposes.
     * Never exposes the actual hash in logs.
     */
    @Override
    public String toString() {
        return "[PROTECTED]";
    }
}

