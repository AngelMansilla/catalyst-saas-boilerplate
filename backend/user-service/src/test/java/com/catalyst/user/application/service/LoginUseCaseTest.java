package com.catalyst.user.application.service;

import com.catalyst.shared.application.ports.JwtTokenPort;
import com.catalyst.shared.domain.auth.LoginRequest;
import com.catalyst.shared.domain.auth.LoginResponse;
import com.catalyst.user.application.ports.output.EventPublisher;
import com.catalyst.user.application.ports.output.PasswordEncoder;
import com.catalyst.user.application.ports.output.PasswordResetTokenRepository;
import com.catalyst.user.application.ports.output.RefreshTokenPort;
import com.catalyst.user.application.ports.output.UserRepository;
import com.catalyst.user.domain.exception.InvalidPasswordException;
import com.catalyst.user.domain.exception.UserNotActiveException;
import com.catalyst.user.domain.model.AuthProvider;
import com.catalyst.user.domain.model.User;
import com.catalyst.user.domain.valueobject.Email;
import com.catalyst.user.domain.valueobject.HashedPassword;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for LoginUseCase implementation in UserApplicationService.
 *
 * <p>Covers all 6 login scenarios defined in T-13 / TR-1.1.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LoginUseCase")
class LoginUseCaseTest {

    private static final String VALID_EMAIL = "jane@example.com";
    private static final String VALID_PASSWORD = "Secret1@";
    private static final String HASHED_PASSWORD = "$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.W7j7W3L4X8OM2e";
    private static final String IP_ADDRESS = "127.0.0.1";
    private static final String ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiJ9.test.token";
    private static final String REFRESH_TOKEN = "opaque-refresh-token-abc123";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private JwtTokenPort jwtTokenPort;

    @Mock
    private RefreshTokenPort refreshTokenPort;

    private UserApplicationService service;

    @BeforeEach
    void setUp() {
        service = new UserApplicationService(
                userRepository,
                tokenRepository,
                passwordEncoder,
                eventPublisher,
                jwtTokenPort,
                refreshTokenPort);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Scenario 1: Valid credentials → returns LoginResponse with tokens
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should return LoginResponse with non-null tokens for valid credentials")
    void shouldReturnLoginResponseForValidCredentials() {
        // Given
        User user = createActiveLocalUser();
        LoginRequest request = LoginRequest.of(VALID_EMAIL, VALID_PASSWORD);

        when(userRepository.findByEmail(Email.of(VALID_EMAIL))).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(eq(VALID_PASSWORD), any(HashedPassword.class))).thenReturn(true);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtTokenPort.generateAccessToken(any(UUID.class), anyString(), any())).thenReturn(ACCESS_TOKEN);
        when(jwtTokenPort.generateRefreshToken()).thenReturn(REFRESH_TOKEN);
        when(jwtTokenPort.getRefreshTokenExpirationSeconds()).thenReturn(604800L);
        when(jwtTokenPort.getAccessTokenExpirationSeconds()).thenReturn(900L);

        // When
        LoginResponse response = service.login(request, IP_ADDRESS);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isGreaterThan(0);
        assertThat(response.user()).isNotNull();
        assertThat(response.user().email()).isEqualTo(VALID_EMAIL);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Scenario 2: Wrong password → throws InvalidPasswordException
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should throw InvalidPasswordException for wrong password")
    void shouldThrowForWrongPassword() {
        // Given
        User user = createActiveLocalUser();
        LoginRequest request = LoginRequest.of(VALID_EMAIL, "WrongPass1!");

        when(userRepository.findByEmail(Email.of(VALID_EMAIL))).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(eq("WrongPass1!"), any(HashedPassword.class))).thenReturn(false);

        // When / Then
        assertThatThrownBy(() -> service.login(request, IP_ADDRESS))
                .isInstanceOf(InvalidPasswordException.class);

        verify(refreshTokenPort, never()).store(anyString(), any(UUID.class), anyLong());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Scenario 3: Unknown email → throws InvalidPasswordException (no enumeration)
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should throw InvalidPasswordException for unknown email (no enumeration)")
    void shouldThrowForUnknownEmail() {
        // Given
        LoginRequest request = LoginRequest.of("unknown@example.com", VALID_PASSWORD);

        when(userRepository.findByEmail(Email.of("unknown@example.com"))).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.login(request, IP_ADDRESS))
                .isInstanceOf(InvalidPasswordException.class);

        verify(refreshTokenPort, never()).store(anyString(), any(UUID.class), anyLong());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Scenario 4: Inactive user → throws UserNotActiveException
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should throw UserNotActiveException for inactive user")
    void shouldThrowForInactiveUser() {
        // Given
        User user = createActiveLocalUser();
        user.deactivate();
        LoginRequest request = LoginRequest.of(VALID_EMAIL, VALID_PASSWORD);

        when(userRepository.findByEmail(Email.of(VALID_EMAIL))).thenReturn(Optional.of(user));

        // When / Then
        assertThatThrownBy(() -> service.login(request, IP_ADDRESS))
                .isInstanceOf(UserNotActiveException.class);

        verify(refreshTokenPort, never()).store(anyString(), any(UUID.class), anyLong());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Scenario 5: OAuth-only user → throws InvalidPasswordException
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should throw InvalidPasswordException for OAuth-only user (canAuthenticateWithPassword = false)")
    void shouldThrowForOAuthUser() {
        // Given
        User oauthUser = User.registerWithOAuth(
                Email.of(VALID_EMAIL),
                "Jane OAuth",
                null,
                AuthProvider.GOOGLE,
                "google-12345");
        LoginRequest request = LoginRequest.of(VALID_EMAIL, VALID_PASSWORD);

        when(userRepository.findByEmail(Email.of(VALID_EMAIL))).thenReturn(Optional.of(oauthUser));

        // When / Then
        assertThatThrownBy(() -> service.login(request, IP_ADDRESS))
                .isInstanceOf(InvalidPasswordException.class);

        verify(refreshTokenPort, never()).store(anyString(), any(UUID.class), anyLong());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Scenario 6: On success → RefreshTokenPort.store() invoked exactly once
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should store refresh token in Redis on successful login")
    void shouldStoreRefreshTokenInRedis() {
        // Given
        User user = createActiveLocalUser();
        LoginRequest request = LoginRequest.of(VALID_EMAIL, VALID_PASSWORD);

        when(userRepository.findByEmail(Email.of(VALID_EMAIL))).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(eq(VALID_PASSWORD), any(HashedPassword.class))).thenReturn(true);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtTokenPort.generateAccessToken(any(UUID.class), anyString(), any())).thenReturn(ACCESS_TOKEN);
        when(jwtTokenPort.generateRefreshToken()).thenReturn(REFRESH_TOKEN);
        when(jwtTokenPort.getRefreshTokenExpirationSeconds()).thenReturn(604800L);
        when(jwtTokenPort.getAccessTokenExpirationSeconds()).thenReturn(900L);

        // When
        service.login(request, IP_ADDRESS);

        // Then
        ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<UUID> userIdCaptor = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<Long> ttlCaptor = ArgumentCaptor.forClass(Long.class);

        verify(refreshTokenPort, times(1)).store(
                tokenCaptor.capture(),
                userIdCaptor.capture(),
                ttlCaptor.capture());

        assertThat(tokenCaptor.getValue()).isNotBlank();
        assertThat(userIdCaptor.getValue()).isNotNull();
        assertThat(ttlCaptor.getValue()).isGreaterThan(0);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────────────────────

    private User createActiveLocalUser() {
        return User.registerWithCredentials(
                Email.of(VALID_EMAIL),
                "Jane Smith",
                HashedPassword.fromHash(HASHED_PASSWORD));
    }
}
