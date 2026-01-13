package com.catalyst.user.application.ports.output;

import com.catalyst.user.domain.valueobject.HashedPassword;

/**
 * Output port for password encoding operations.
 * Abstracts the password hashing implementation from the domain.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
public interface PasswordEncoder {
    
    /**
     * Encodes a raw password using BCrypt.
     * 
     * @param rawPassword the plain text password
     * @return the hashed password
     */
    HashedPassword encode(String rawPassword);
    
    /**
     * Validates a raw password against a hashed password.
     * 
     * @param rawPassword the plain text password to check
     * @param hashedPassword the stored hash
     * @return true if passwords match
     */
    boolean matches(String rawPassword, HashedPassword hashedPassword);
}

