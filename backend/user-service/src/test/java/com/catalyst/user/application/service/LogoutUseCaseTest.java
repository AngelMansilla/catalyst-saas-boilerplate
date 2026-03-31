package com.catalyst.user.application.service;

import com.catalyst.shared.application.ports.JwtTokenPort;
import com.catalyst.user.application.ports.output.EventPublisher;
import com.catalyst.user.application.ports.output.PasswordEncoder;
import com.catalyst.user.application.ports.output.PasswordResetTokenRepository;
import com.catalyst.user.application.ports.output.RefreshTokenPort;
import com.catalyst.user.application.ports.output.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for LogoutUseCase implementation in UserApplicationService.
 *
 * <p>Covers 2 scenarios defined in T-15 / TR-1.3.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LogoutUseCase")
class LogoutUseCaseTest {

    private static final String REFRESH_TOKEN = "some-opaque-refresh-token";

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
    // Scenario 1: logout() → RefreshTokenPort.delete() called exactly once
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should delete refresh token on logout")
    void shouldDeleteRefreshTokenOnLogout() {
        // Given — delete is a void method; Mockito does nothing by default

        // When
        service.logout(REFRESH_TOKEN);

        // Then
        verify(refreshTokenPort, times(1)).delete(REFRESH_TOKEN);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Scenario 2: Token not in Redis → no exception thrown (idempotent)
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should succeed without throwing even if token does not exist in Redis")
    void shouldSucceedEvenIfTokenNotInRedis() {
        // Given — delete() is a no-op (mock does nothing by default)
        doNothing().when(refreshTokenPort).delete(REFRESH_TOKEN);

        // When / Then — no exception should propagate
        assertThatNoException().isThrownBy(() -> service.logout(REFRESH_TOKEN));

        verify(refreshTokenPort, times(1)).delete(REFRESH_TOKEN);
    }
}
