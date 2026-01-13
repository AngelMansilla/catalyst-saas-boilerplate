package com.catalyst.user.infrastructure.persistence.mapper;

import com.catalyst.user.domain.model.AuthProvider;
import com.catalyst.user.domain.model.User;
import com.catalyst.user.domain.model.UserRole;
import com.catalyst.user.domain.valueobject.Email;
import com.catalyst.user.domain.valueobject.HashedPassword;
import com.catalyst.user.domain.valueobject.UserId;
import com.catalyst.user.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper between User domain entity and UserJpaEntity.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
@Component
public class UserMapper {
    
    /**
     * Maps a JPA entity to a domain entity.
     */
    public User toDomain(UserJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return User.reconstitute(
            UserId.of(entity.getId()),
            Email.of(entity.getEmail()),
            entity.getName(),
            entity.getImageUrl(),
            HashedPassword.fromTrustedHash(entity.getPasswordHash()),
            AuthProvider.fromCode(entity.getProvider()),
            entity.getProviderAccountId(),
            UserRole.fromCode(entity.getRole()),
            entity.isEmailVerified(),
            entity.isActive(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getLastLoginAt()
        );
    }
    
    /**
     * Maps a domain entity to a JPA entity.
     */
    public UserJpaEntity toJpa(User user) {
        if (user == null) {
            return null;
        }
        
        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(user.getId().getValue());
        entity.setEmail(user.getEmail().getValue());
        entity.setName(user.getName());
        entity.setImageUrl(user.getImageUrl());
        entity.setPasswordHash(user.getPasswordHash() != null ? user.getPasswordHash().getValue() : null);
        entity.setProvider(user.getProvider().getCode());
        entity.setProviderAccountId(user.getProviderAccountId());
        entity.setRole(user.getRole().getCode());
        entity.setEmailVerified(user.isEmailVerified());
        entity.setActive(user.isActive());
        entity.setCreatedAt(user.getCreatedAt());
        entity.setUpdatedAt(user.getUpdatedAt());
        entity.setLastLoginAt(user.getLastLoginAt());
        return entity;
    }
    
    /**
     * Updates an existing JPA entity from a domain entity.
     */
    public void updateJpa(UserJpaEntity entity, User user) {
        entity.setName(user.getName());
        entity.setImageUrl(user.getImageUrl());
        entity.setPasswordHash(user.getPasswordHash() != null ? user.getPasswordHash().getValue() : null);
        entity.setRole(user.getRole().getCode());
        entity.setEmailVerified(user.isEmailVerified());
        entity.setActive(user.isActive());
        entity.setLastLoginAt(user.getLastLoginAt());
        // updatedAt is handled by @PreUpdate
    }
}

