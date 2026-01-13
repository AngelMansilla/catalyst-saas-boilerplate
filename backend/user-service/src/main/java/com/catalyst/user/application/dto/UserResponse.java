package com.catalyst.user.application.dto;

import com.catalyst.user.domain.model.AuthProvider;
import com.catalyst.user.domain.model.User;
import com.catalyst.user.domain.model.UserRole;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO representing user information.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
public record UserResponse(
        UUID id,
        String email,
        String name,
        String imageUrl,
        String provider,
        String role,
        boolean emailVerified,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime lastLoginAt
) {
    
    /**
     * Creates a UserResponse from a domain User entity.
     * 
     * @param user the domain user
     * @return the response DTO
     */
    public static UserResponse fromDomain(User user) {
        return new UserResponse(
            user.getId().getValue(),
            user.getEmail().getValue(),
            user.getName(),
            user.getImageUrl(),
            user.getProvider().getCode(),
            user.getRole().getCode(),
            user.isEmailVerified(),
            user.isActive(),
            user.getCreatedAt(),
            user.getLastLoginAt()
        );
    }
}

