package com.catalyst.user.infrastructure.security;

import com.catalyst.user.application.ports.output.PasswordEncoder;
import com.catalyst.user.domain.valueobject.HashedPassword;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * BCrypt implementation of the PasswordEncoder port.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
@Component
public class BcryptPasswordEncoderAdapter implements PasswordEncoder {
    
    private final BCryptPasswordEncoder bcrypt;
    
    public BcryptPasswordEncoderAdapter() {
        // Strength 12 is a good balance between security and performance
        this.bcrypt = new BCryptPasswordEncoder(12);
    }
    
    @Override
    public HashedPassword encode(String rawPassword) {
        String hash = bcrypt.encode(rawPassword);
        return HashedPassword.fromHash(hash);
    }
    
    @Override
    public boolean matches(String rawPassword, HashedPassword hashedPassword) {
        if (hashedPassword == null || hashedPassword.getValue() == null) {
            return false;
        }
        return bcrypt.matches(rawPassword, hashedPassword.getValue());
    }
}

