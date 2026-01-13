package com.catalyst.user.infrastructure.persistence.adapter;

import com.catalyst.user.application.ports.output.PasswordResetTokenRepository;
import com.catalyst.user.domain.model.PasswordResetToken;
import com.catalyst.user.domain.valueobject.UserId;
import com.catalyst.user.infrastructure.persistence.mapper.PasswordResetTokenMapper;
import com.catalyst.user.infrastructure.persistence.repository.PasswordResetTokenJpaRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Adapter implementing PasswordResetTokenRepository port using JPA.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
@Component
public class PasswordResetTokenRepositoryAdapter implements PasswordResetTokenRepository {
    
    private final PasswordResetTokenJpaRepository jpaRepository;
    private final PasswordResetTokenMapper mapper;
    
    public PasswordResetTokenRepositoryAdapter(
            PasswordResetTokenJpaRepository jpaRepository,
            PasswordResetTokenMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }
    
    @Override
    public PasswordResetToken save(PasswordResetToken token) {
        var entity = mapper.toJpa(token);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
    
    @Override
    public Optional<PasswordResetToken> findByToken(String token) {
        return jpaRepository.findByToken(token)
                .map(mapper::toDomain);
    }
    
    @Override
    public Optional<PasswordResetToken> findLatestValidByUserId(UserId userId) {
        return jpaRepository.findLatestValidByUserId(userId.getValue(), LocalDateTime.now())
                .map(mapper::toDomain);
    }
    
    @Override
    public void invalidateAllForUser(UserId userId) {
        jpaRepository.invalidateAllForUser(userId.getValue(), LocalDateTime.now());
    }
    
    @Override
    public int deleteExpiredAndUsed() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoff = now.minusDays(7);
        return jpaRepository.deleteExpiredAndUsed(now, cutoff);
    }
}

