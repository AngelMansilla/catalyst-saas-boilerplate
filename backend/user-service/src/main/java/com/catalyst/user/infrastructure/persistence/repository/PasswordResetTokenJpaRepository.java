package com.catalyst.user.infrastructure.persistence.repository;

import com.catalyst.user.infrastructure.persistence.entity.PasswordResetTokenJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for password reset tokens.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
@Repository
public interface PasswordResetTokenJpaRepository extends JpaRepository<PasswordResetTokenJpaEntity, UUID> {
    
    Optional<PasswordResetTokenJpaEntity> findByToken(String token);
    
    @Query("SELECT t FROM PasswordResetTokenJpaEntity t " +
           "WHERE t.userId = :userId AND t.used = false AND t.expiresAt > :now " +
           "ORDER BY t.createdAt DESC LIMIT 1")
    Optional<PasswordResetTokenJpaEntity> findLatestValidByUserId(
            @Param("userId") UUID userId, 
            @Param("now") LocalDateTime now);
    
    @Modifying
    @Query("UPDATE PasswordResetTokenJpaEntity t SET t.used = true, t.usedAt = :now " +
           "WHERE t.userId = :userId AND t.used = false")
    void invalidateAllForUser(@Param("userId") UUID userId, @Param("now") LocalDateTime now);
    
    @Modifying
    @Query("DELETE FROM PasswordResetTokenJpaEntity t " +
           "WHERE t.expiresAt < :now OR (t.used = true AND t.usedAt < :cutoff)")
    int deleteExpiredAndUsed(@Param("now") LocalDateTime now, @Param("cutoff") LocalDateTime cutoff);
}

