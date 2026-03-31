package com.catalyst.user.application.service;

import com.catalyst.shared.application.ports.JwtTokenPort;
import com.catalyst.shared.domain.auth.LoginResponse;
import com.catalyst.shared.domain.auth.TokenResponse;
import com.catalyst.shared.domain.exception.AuthenticationException;
import com.catalyst.user.application.dto.*;
import com.catalyst.user.application.ports.input.*;
import com.catalyst.user.application.ports.output.*;
import com.catalyst.user.domain.event.*;
import com.catalyst.user.domain.exception.*;
import com.catalyst.user.domain.model.*;
import com.catalyst.user.domain.valueobject.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Application service implementing all user-related use cases.
 * 
 * <p>
 * This service orchestrates domain logic, persistence, and event publishing.
 * It implements all input ports defined in the application layer.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
@Service
@Transactional
public class UserApplicationService implements
        RegisterUserUseCase,
        ValidateCredentialsUseCase,
        SyncSocialUserUseCase,
        RequestPasswordResetUseCase,
        ResetPasswordUseCase,
        GetUserProfileUseCase,
        UpdateUserProfileUseCase,
        DeleteUserUseCase,
        LoginUseCase,
        RefreshTokenUseCase,
        LogoutUseCase {

    private static final Logger log = LoggerFactory.getLogger(UserApplicationService.class);

    // Password must have at least 1 uppercase, 1 lowercase, 1 number, 1 special
    // char
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EventPublisher eventPublisher;
    private final JwtTokenPort jwtTokenPort;
    private final RefreshTokenPort refreshTokenPort;

    @Value("${user.password-reset.token-expiration-hours:24}")
    private int tokenExpirationHours;

    public UserApplicationService(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            EventPublisher eventPublisher,
            JwtTokenPort jwtTokenPort,
            RefreshTokenPort refreshTokenPort) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
        this.jwtTokenPort = jwtTokenPort;
        this.refreshTokenPort = refreshTokenPort;
    }

    // ========================
    // RegisterUserUseCase
    // ========================

    @Override
    public UserResponse register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.email());

        Email email = Email.of(request.email());

        // Check if email already exists
        if (userRepository.existsByEmail(email)) {
            log.warn("Registration failed: email already exists - {}", request.email());
            throw new EmailAlreadyExistsException(email);
        }

        // Validate password strength
        validatePasswordStrength(request.password());

        // Hash password
        HashedPassword hashedPassword = passwordEncoder.encode(request.password());

        // Create user
        User user = User.registerWithCredentials(email, request.name(), hashedPassword);

        // Save user
        User savedUser = userRepository.save(user);

        // Publish domain events from the original entity
        user.getDomainEvents().forEach(event -> {
            if (event instanceof com.catalyst.user.domain.event.UserRegistered userRegistered) {
                eventPublisher.publish(userRegistered);
            }
        });

        log.info("User registered successfully: {}", savedUser.getId());

        return UserResponse.fromDomain(savedUser);
    }

    // ========================
    // ValidateCredentialsUseCase
    // ========================

    @Override
    public UserResponse validate(LoginRequest request, String ipAddress) {
        log.debug("Validating credentials for email: {}", request.email());

        Email email = Email.of(request.email());

        // Find user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Login failed: user not found - {}", request.email());
                    return InvalidPasswordException.invalidCredentials();
                });

        // Check if user can authenticate with password
        if (!user.canAuthenticateWithPassword()) {
            log.warn("Login failed: user uses OAuth provider - {}", request.email());
            throw new InvalidPasswordException("Please use " + user.getProvider().getDisplayName() + " to sign in");
        }

        // Check if user is active
        if (!user.isActive()) {
            log.warn("Login failed: user not active - {}", request.email());
            throw new UserNotActiveException();
        }

        // Validate password
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.warn("Login failed: invalid password - {}", request.email());
            throw InvalidPasswordException.invalidCredentials();
        }

        // Record login
        user.recordLogin(ipAddress);
        User updatedUser = userRepository.save(user);

        // Publish login event from the original entity
        user.getDomainEvents().forEach(event -> {
            if (event instanceof com.catalyst.user.domain.event.UserLoggedIn userLoggedIn) {
                eventPublisher.publish(userLoggedIn);
            }
        });

        log.info("User authenticated successfully: {}", user.getId());

        return UserResponse.fromDomain(updatedUser);
    }

    // ========================
    // SyncSocialUserUseCase
    // ========================

    @Override
    public UserResponse sync(SyncUserRequest request) {
        log.info("Syncing OAuth user: {} from {}", request.email(), request.provider());

        Email email = Email.of(request.email());
        AuthProvider provider = AuthProvider.fromCode(request.provider());

        // Try to find existing user by provider account ID
        Optional<User> existingByProvider = userRepository
                .findByProviderAndProviderAccountId(provider.getCode(), request.providerAccountId());

        if (existingByProvider.isPresent()) {
            // Update existing user
            User user = existingByProvider.get();
            log.info("Social user already exists (by provider ID), syncing: {}", user.getId());
            user.syncFromOAuth(request.name(), request.imageUrl());
            User savedUser = userRepository.save(user);
            log.info("Existing OAuth user synced: {}", savedUser.getId());
            return UserResponse.fromDomain(savedUser);
        }

        // Check if email exists with different provider
        Optional<User> existingByEmail = userRepository.findByEmail(email);
        if (existingByEmail.isPresent()) {
            User user = existingByEmail.get();
            log.warn("Email {} already registered with provider {}",
                    request.email(), user.getProvider());
            throw new EmailAlreadyExistsException(email);
        }

        log.info("Creating NEW OAuth user for email: {}", request.email());
        // Create new OAuth user
        User newUser = User.registerWithOAuth(
                email,
                request.name(),
                request.imageUrl(),
                provider,
                request.providerAccountId());

        User savedUser = userRepository.save(newUser);

        // Publish registration event from the new user entity
        log.info("Found {} domain events to publish", newUser.getDomainEvents().size());
        newUser.getDomainEvents().forEach(event -> {
            if (event instanceof com.catalyst.user.domain.event.UserRegistered userRegistered) {
                log.info("Publishing UserRegistered event for sync: {}", userRegistered.email().getValue());
                eventPublisher.publish(userRegistered);
            }
        });

        log.info("New OAuth user created: {}", savedUser.getId());

        return UserResponse.fromDomain(savedUser);
    }

    // ========================
    // RequestPasswordResetUseCase
    // ========================

    @Override
    public void requestReset(PasswordResetRequest request, String ipAddress) {
        log.info("Password reset requested for: {}", request.email());

        Email email;
        try {
            email = Email.of(request.email());
        } catch (IllegalArgumentException e) {
            // Invalid email format - silently return to prevent enumeration
            log.debug("Invalid email format in password reset request");
            return;
        }

        // Find user - but don't reveal if they exist
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            // User doesn't exist - silently return
            log.debug("Password reset requested for non-existent user");
            return;
        }

        User user = userOpt.get();

        // Can't reset password for OAuth users
        if (user.getProvider().isSocial()) {
            log.debug("Password reset requested for OAuth user - ignoring");
            return;
        }

        // Create reset token
        PasswordResetToken resetToken = PasswordResetToken.create(
                user.getId(),
                tokenExpirationHours,
                ipAddress);

        tokenRepository.save(resetToken);

        // Notify user domain to emit event
        user.requestPasswordReset(resetToken.getToken(), resetToken.getExpiresAt());

        // Publish event for notification service
        user.getDomainEvents().forEach(event -> {
            if (event instanceof com.catalyst.user.domain.event.PasswordResetRequested resetRequested) {
                eventPublisher.publish(resetRequested);
            }
        });

        log.info("Password reset token created for user: {}", user.getId());
    }

    // ========================
    // ResetPasswordUseCase
    // ========================

    @Override
    public void resetPassword(NewPasswordRequest request) {
        log.info("Attempting password reset with token");

        // Find token
        PasswordResetToken resetToken = tokenRepository.findByToken(request.token())
                .orElseThrow(() -> {
                    log.warn("Password reset failed: invalid token");
                    return InvalidResetTokenException.notFound();
                });

        // Validate token
        if (resetToken.isUsed()) {
            log.warn("Password reset failed: token already used");
            throw InvalidResetTokenException.alreadyUsed();
        }

        if (resetToken.isExpired()) {
            log.warn("Password reset failed: token expired");
            throw InvalidResetTokenException.expired();
        }

        // Validate new password
        validatePasswordStrength(request.newPassword());

        // Find user
        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new UserNotFoundException(resetToken.getUserId()));

        // Hash new password
        HashedPassword newHashedPassword = passwordEncoder.encode(request.newPassword());

        // Update password
        user.changePassword(newHashedPassword);
        userRepository.save(user);

        // Mark token as used
        resetToken.use();
        tokenRepository.save(resetToken);

        // Invalidate all other tokens for this user
        tokenRepository.invalidateAllForUser(user.getId());

        // Publish event
        user.getDomainEvents().forEach(event -> {
            if (event instanceof com.catalyst.user.domain.event.PasswordResetCompleted resetCompleted) {
                eventPublisher.publish(resetCompleted);
            }
        });

        log.info("Password reset successfully for user: {}", user.getId());
    }

    // ========================
    // GetUserProfileUseCase
    // ========================

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(UserId userId) {
        log.debug("Getting user by ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        return UserResponse.fromDomain(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserResponse> findByEmail(String emailStr) {
        log.debug("Finding user by email: {}", emailStr);

        try {
            Email email = Email.of(emailStr);
            return userRepository.findByEmail(email)
                    .map(UserResponse::fromDomain);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> listAll(int page, int size) {
        log.debug("Listing all users: page={}, size={}", page, size);

        return userRepository.findAll(page, size).stream()
                .map(UserResponse::fromDomain)
                .toList();
    }

    // ========================
    // UpdateUserProfileUseCase
    // ========================

    @Override
    public UserResponse updateProfile(UserId userId, UpdateProfileRequest request) {
        log.info("Updating profile for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        user.updateProfile(
                request.name() != null ? request.name() : user.getName(),
                request.imageUrl() != null ? request.imageUrl() : user.getImageUrl());

        User savedUser = userRepository.save(user);

        log.info("Profile updated for user: {}", userId);

        return UserResponse.fromDomain(savedUser);
    }

    // ========================
    // DeleteUserUseCase
    // ========================

    @Override
    public void deleteUser(UserId userId) {
        log.warn("PERMANENTLY deleting user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // Final event before deletion
        UserDeleted deletionEvent = new UserDeleted(user.getId(), user.getEmail());

        // Delete tokens first
        tokenRepository.invalidateAllForUser(userId);

        // Delete user
        userRepository.delete(userId);

        // Publish event to inform other services
        eventPublisher.publish(deletionEvent);

        log.info("User {} deleted successfully", userId);
    }

    // ========================
    // LoginUseCase
    // ========================

    @Override
    public LoginResponse login(com.catalyst.shared.domain.auth.LoginRequest request, String ipAddress) {
        log.info("Login attempt for email: {}", request.email());

        Email email = Email.of(request.email());

        // Find user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Login failed: user not found - {}", request.email());
                    return InvalidPasswordException.invalidCredentials();
                });

        // Check if user can authenticate with password (not OAuth-only)
        if (!user.canAuthenticateWithPassword()) {
            log.warn("Login failed: user uses OAuth provider - {}", request.email());
            throw new InvalidPasswordException("Please use " + user.getProvider().getDisplayName() + " to sign in");
        }

        // Check if user is active
        if (!user.isActive()) {
            log.warn("Login failed: user not active - {}", request.email());
            throw new UserNotActiveException();
        }

        // Validate password
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.warn("Login failed: invalid password - {}", request.email());
            throw InvalidPasswordException.invalidCredentials();
        }

        // Record login and save
        user.recordLogin(ipAddress);
        User updatedUser = userRepository.save(user);

        // Publish login event
        user.getDomainEvents().forEach(event -> {
            if (event instanceof UserLoggedIn userLoggedIn) {
                eventPublisher.publish(userLoggedIn);
            }
        });

        // Generate tokens
        UUID userId = updatedUser.getId().getValue();
        Set<String> roles = Set.of(updatedUser.getRole().getAuthority());
        String accessToken = jwtTokenPort.generateAccessToken(userId, updatedUser.getEmail().getValue(), roles);
        String refreshToken = jwtTokenPort.generateRefreshToken();
        long ttlSeconds = jwtTokenPort.getRefreshTokenExpirationSeconds();

        // Store refresh token in Redis
        refreshTokenPort.store(refreshToken, userId, ttlSeconds);

        log.info("User logged in successfully: {}", userId);

        return LoginResponse.of(
                accessToken,
                refreshToken,
                jwtTokenPort.getAccessTokenExpirationSeconds(),
                LoginResponse.UserInfo.of(userId, updatedUser.getEmail().getValue(), updatedUser.getName(), roles)
        );
    }

    // ========================
    // RefreshTokenUseCase
    // ========================

    @Override
    public TokenResponse refresh(String refreshToken) {
        log.debug("Refresh token request");

        // Look up userId from Redis
        UUID userId = refreshTokenPort.findUserIdByToken(refreshToken)
                .orElseThrow(() -> {
                    log.warn("Refresh failed: token not found in Redis");
                    return AuthenticationException.invalidToken();
                });

        // Load user from repository
        User user = userRepository.findById(UserId.of(userId))
                .orElseThrow(() -> {
                    log.warn("Refresh failed: user not found for id {}", userId);
                    return AuthenticationException.invalidToken();
                });

        // Check user is still active
        if (!user.isActive()) {
            log.warn("Refresh failed: user not active - {}", userId);
            throw AuthenticationException.invalidToken();
        }

        // Generate new token pair
        Set<String> roles = Set.of(user.getRole().getAuthority());
        String newAccessToken = jwtTokenPort.generateAccessToken(userId, user.getEmail().getValue(), roles);
        String newRefreshToken = jwtTokenPort.generateRefreshToken();
        long ttlSeconds = jwtTokenPort.getRefreshTokenExpirationSeconds();

        // Rotate: revoke old, store new
        refreshTokenPort.delete(refreshToken);
        refreshTokenPort.store(newRefreshToken, userId, ttlSeconds);

        log.info("Tokens refreshed for user: {}", userId);

        return TokenResponse.of(newAccessToken, newRefreshToken, jwtTokenPort.getAccessTokenExpirationSeconds());
    }

    // ========================
    // LogoutUseCase
    // ========================

    @Override
    public void logout(String refreshToken) {
        log.debug("Logout request — revoking refresh token");
        refreshTokenPort.delete(refreshToken);
        log.info("Refresh token revoked");
    }

    // ========================
    // Validation Helpers
    // ========================

    private void validatePasswordStrength(String password) {
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw InvalidPasswordException.weakPassword();
        }
    }
}
