package com.catalyst.user.application.ports.input;

import com.catalyst.user.application.dto.RegisterRequest;
import com.catalyst.user.application.dto.UserResponse;

/**
 * Use case for registering a new user with email/password.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
public interface RegisterUserUseCase {
    
    /**
     * Registers a new user with email and password credentials.
     * 
     * @param request the registration data
     * @return the created user information
     * @throws com.catalyst.user.domain.exception.EmailAlreadyExistsException if email is taken
     * @throws com.catalyst.user.domain.exception.InvalidPasswordException if password is weak
     */
    UserResponse register(RegisterRequest request);
}

