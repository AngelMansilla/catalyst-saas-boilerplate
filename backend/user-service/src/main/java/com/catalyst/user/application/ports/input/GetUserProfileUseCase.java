package com.catalyst.user.application.ports.input;

import com.catalyst.user.application.dto.UserResponse;
import com.catalyst.user.domain.valueobject.UserId;

import java.util.List;
import java.util.Optional;

/**
 * Use case for retrieving user profile information.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
public interface GetUserProfileUseCase {
    
    /**
     * Gets a user by their ID.
     * 
     * @param userId the user ID
     * @return the user information
     * @throws com.catalyst.user.domain.exception.UserNotFoundException if not found
     */
    UserResponse getById(UserId userId);
    
    /**
     * Gets a user by email if exists.
     * 
     * @param email the email address
     * @return optional user information
     */
    Optional<UserResponse> findByEmail(String email);
    
    /**
     * Lists all users (admin only).
     * 
     * @param page page number (0-based)
     * @param size page size
     * @return list of users
     */
    List<UserResponse> listAll(int page, int size);
}

