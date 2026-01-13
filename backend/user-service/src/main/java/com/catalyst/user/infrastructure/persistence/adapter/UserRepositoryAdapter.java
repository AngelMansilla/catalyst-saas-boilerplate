package com.catalyst.user.infrastructure.persistence.adapter;

import com.catalyst.user.application.ports.output.UserRepository;
import com.catalyst.user.domain.model.User;
import com.catalyst.user.domain.valueobject.Email;
import com.catalyst.user.domain.valueobject.UserId;
import com.catalyst.user.infrastructure.persistence.entity.UserJpaEntity;
import com.catalyst.user.infrastructure.persistence.mapper.UserMapper;
import com.catalyst.user.infrastructure.persistence.repository.UserJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adapter implementing UserRepository port using JPA.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
@Component
public class UserRepositoryAdapter implements UserRepository {
    
    private final UserJpaRepository jpaRepository;
    private final UserMapper mapper;
    
    public UserRepositoryAdapter(UserJpaRepository jpaRepository, UserMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }
    
    @Override
    public User save(User user) {
        // Check if user exists (update) or is new (create)
        Optional<UserJpaEntity> existing = jpaRepository.findById(user.getId().getValue());
        
        UserJpaEntity entity;
        if (existing.isPresent()) {
            entity = existing.get();
            mapper.updateJpa(entity, user);
        } else {
            entity = mapper.toJpa(user);
        }
        
        UserJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
    
    @Override
    public Optional<User> findById(UserId userId) {
        return jpaRepository.findById(userId.getValue())
                .map(mapper::toDomain);
    }
    
    @Override
    public Optional<User> findByEmail(Email email) {
        return jpaRepository.findByEmail(email.getValue())
                .map(mapper::toDomain);
    }
    
    @Override
    public Optional<User> findByProviderAndProviderAccountId(String provider, String providerAccountId) {
        return jpaRepository.findByProviderAndProviderAccountId(provider, providerAccountId)
                .map(mapper::toDomain);
    }
    
    @Override
    public boolean existsByEmail(Email email) {
        return jpaRepository.existsByEmail(email.getValue());
    }
    
    @Override
    public List<User> findAll(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return jpaRepository.findAll(pageRequest)
                .getContent()
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
    
    @Override
    public long count() {
        return jpaRepository.count();
    }
    
    @Override
    public void delete(User user) {
        jpaRepository.deleteById(user.getId().getValue());
    }
}

