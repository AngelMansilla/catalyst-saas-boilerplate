package com.catalyst.user.application.service;

import com.catalyst.shared.application.ports.JwtTokenPort;
import com.catalyst.shared.domain.auth.TokenResponse;
import com.catalyst.shared.domain.exception.AuthenticationException;
import com.catalyst.user.application.ports.output.EventPublisher;
import com.catalyst.user.application.ports.output.PasswordEncoder;
import com.catalyst.user.application.ports.output.PasswordResetTokenRepository;
import com.catalyst.user.application.ports.output.RefreshTokenPort;
import com.catalyst.user.application.ports.output.UserRepository;
import com.catalyst.user.domain.model.User;
import com.catalyst.user.domain.valueobject.Email;
import com.catalyst.user.domain.valueobject.HashedPassword;
import com.catalyst.user.domain.valueobject.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
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
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for RefreshTokenUseCase implementation in UserApplicationService.
 *
 * <p>Covers 4 scenarios defined in T-14 / TR-1.2.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenUseCase")
class RefreshTokenUseCaseTest {

    private static final String OLD_REFRESH_TOKEN = "old-opaque-refresh-token";
    private static final String NEW_REFRESH_TOKEN = "new-opaque-refresh-token";
    private static final String NEW_ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiJ9.new.token";
    private static final String HASHED_PASSWORD = "$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.W7j7W3L4X8OM2e";

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
    // Scenario 1: Valid refresh token → returns TokenResponse with new tokens
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should return new token pair for valid refresh token")
    void shouldReturnNewTokenPairForValidRefreshToken() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = createActiveUser(userId);

        when(refreshTokenPort.findUserIdByToken(OLD_REFRESH_TOKEN)).thenReturn(Optional.of(userId));
        when(userRepository.findById(UserId.of(userId))).thenReturn(Optional.of(user));
        when(jwtTokenPort.generateAccessToken(any(UUID.class), anyString(), any())).thenReturn(NEW_ACCESS_TOKEN);
        when(jwtTokenPort.generateRefreshToken()).thenReturn(NEW_REFRESH_TOKEN);
        when(jwtTokenPort.getRefreshTokenExpirationSeconds()).thenReturn(604800L);
        when(jwtTokenPort.getAccessTokenExpirationSeconds()).thenReturn(900L);

        // When
        TokenResponse response = service.refresh(OLD_REFRESH_TOKEN);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isGreaterThan(0);
        // New tokens must differ from the original refresh token
        assertThat(response.refreshToken()).isNotEqualTo(OLD_REFRESH_TOKEN);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Scenario 2: After refresh → old token deleted + new token stored
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should delete old token and store new token during refresh")
    void shouldDeleteOldTokenAndStoreNewToken() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = createActiveUser(userId);

        when(refreshTokenPort.findUserIdByToken(OLD_REFRESH_TOKEN)).thenReturn(Optional.of(userId));
        when(userRepository.findById(UserId.of(userId))).thenReturn(Optional.of(user));
        when(jwtTokenPort.generateAccessToken(any(UUID.class), anyString(), any())).thenReturn(NEW_ACCESS_TOKEN);
        when(jwtTokenPort.generateRefreshToken()).thenReturn(NEW_REFRESH_TOKEN);
        when(jwtTokenPort.getRefreshTokenExpirationSeconds()).thenReturn(604800L);
        when(jwtTokenPort.getAccessTokenExpirationSeconds()).thenReturn(900L);

        // When
        service.refresh(OLD_REFRESH_TOKEN);

        // Then — delete(old) must happen before store(new)
        InOrder order = inOrder(refreshTokenPort);
        order.verify(refreshTokenPort).delete(OLD_REFRESH_TOKEN);
        order.verify(refreshTokenPort).store(NEW_REFRESH_TOKEN, userId, 604800L);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Scenario 3: Unknown/expired token → throws AuthenticationException
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should throw AuthenticationException for unknown or expired refresh token")
    void shouldThrowForUnknownRefreshToken() {
        // Given
        when(refreshTokenPort.findUserIdByToken(OLD_REFRESH_TOKEN)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.refresh(OLD_REFRESH_TOKEN))
                .isInstanceOf(AuthenticationException.class);

        verify(userRepository, never()).findById(any());
        verify(refreshTokenPort, never()).store(anyString(), any(UUID.class), anyLong());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Scenario 4: User not found for token's userId → throws AuthenticationException
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should throw AuthenticationException when user no longer exists for token")
    void shouldThrowForUserNotFound() {
        // Given
        UUID userId = UUID.randomUUID();

        when(refreshTokenPort.findUserIdByToken(OLD_REFRESH_TOKEN)).thenReturn(Optional.of(userId));
        when(userRepository.findById(UserId.of(userId))).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.refresh(OLD_REFRESH_TOKEN))
                .isInstanceOf(AuthenticationException.class);

        verify(refreshTokenPort, never()).store(anyString(), any(UUID.class), anyLong());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────────────────────

    private User createActiveUser(UUID rawUserId) {
        return User.registerWithCredentials(
                Email.of("user@example.com"),
                "Test User",
                HashedPassword.fromHash(HASHED_PASSWORD));
    }
}
