package com.catalyst.user.infrastructure.persistence.repository;

import com.catalyst.user.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for users.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
@Repository
public interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {
    
    Optional<UserJpaEntity> findByEmail(String email);
    
    Optional<UserJpaEntity> findByProviderAndProviderAccountId(String provider, String providerAccountId);
    
    boolean existsByEmail(String email);
    
    Page<UserJpaEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
}

