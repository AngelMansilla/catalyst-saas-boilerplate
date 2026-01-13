package com.catalyst.user.application.ports.input;

import com.catalyst.user.application.dto.LoginRequest;
import com.catalyst.user.application.dto.UserResponse;

/**
 * Use case for validating user credentials.
 * Used by NextAuth's Credentials provider to authenticate users.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
public interface ValidateCredentialsUseCase {
    
    /**
     * Validates user credentials and returns user information if valid.
     * 
     * @param request the login credentials
     * @param ipAddress the IP address of the request (for audit)
     * @return the user information if credentials are valid
     * @throws com.catalyst.user.domain.exception.InvalidPasswordException if credentials invalid
     * @throws com.catalyst.user.domain.exception.UserNotActiveException if account is disabled
     */
    UserResponse validate(LoginRequest request, String ipAddress);
}

