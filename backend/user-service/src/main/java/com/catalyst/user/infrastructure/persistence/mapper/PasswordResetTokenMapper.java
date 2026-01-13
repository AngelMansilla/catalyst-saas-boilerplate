package com.catalyst.user.infrastructure.persistence.mapper;

import com.catalyst.user.domain.model.PasswordResetToken;
import com.catalyst.user.domain.valueobject.UserId;
import com.catalyst.user.infrastructure.persistence.entity.PasswordResetTokenJpaEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper between PasswordResetToken domain entity and JPA entity.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
@Component
public class PasswordResetTokenMapper {
    
    /**
     * Maps a JPA entity to a domain entity.
     */
    public PasswordResetToken toDomain(PasswordResetTokenJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return PasswordResetToken.reconstitute(
            entity.getId(),
            UserId.of(entity.getUserId()),
            entity.getToken(),
            entity.getExpiresAt(),
            entity.isUsed(),
            entity.getUsedAt(),
            entity.getCreatedAt(),
            entity.getIpAddress()
        );
    }
    
    /**
     * Maps a domain entity to a JPA entity.
     */
    public PasswordResetTokenJpaEntity toJpa(PasswordResetToken token) {
        if (token == null) {
            return null;
        }
        
        PasswordResetTokenJpaEntity entity = new PasswordResetTokenJpaEntity();
        entity.setId(token.getId());
        entity.setUserId(token.getUserId().getValue());
        entity.setToken(token.getToken());
        entity.setExpiresAt(token.getExpiresAt());
        entity.setUsed(token.isUsed());
        entity.setUsedAt(token.getUsedAt());
        entity.setCreatedAt(token.getCreatedAt());
        entity.setIpAddress(token.getIpAddress());
        return entity;
    }
}

