package com.catalyst.user.application.ports.input;

import com.catalyst.shared.domain.auth.LoginRequest;
import com.catalyst.shared.domain.auth.LoginResponse;

/**
 * Input port for user authentication via email/password.
 * Returns a token pair and basic user info.
 */
public interface LoginUseCase {

    /**
     * Authenticates a user with email and password.
     * Generates an access token and a refresh token.
     * Stores the refresh token in Redis with TTL.
     *
     * @param request   the login request containing email, password and rememberMe flag
     * @param ipAddress the client IP address for audit and rate limiting
     * @return LoginResponse containing access token, refresh token, expiry and user info
     */
    LoginResponse login(LoginRequest request, String ipAddress);
}
