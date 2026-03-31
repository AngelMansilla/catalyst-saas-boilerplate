package com.catalyst.shared.infrastructure.security;

import com.catalyst.shared.application.ports.JwtTokenPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for JwtAuthenticationFilter.shouldNotFilter() path logic.
 *
 * <p>Covers 5 scenarios defined in T-16 / TR-1.4.
 * Tests verify that auth endpoints are skipped by the filter and that
 * protected paths are still processed.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter — shouldNotFilter()")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenPort jwtTokenPort;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtTokenPort);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Scenario 1: /api/v1/auth/login → shouldNotFilter() returns true
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should not filter login path /api/v1/auth/login")
    void shouldNotFilterLoginPath() throws Exception {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/auth/login");

        // When
        boolean result = filter.shouldNotFilter(request);

        // Then
        assertThat(result).isTrue();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Scenario 2: /api/v1/auth/refresh → shouldNotFilter() returns true
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should not filter refresh path /api/v1/auth/refresh")
    void shouldNotFilterRefreshPath() throws Exception {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/auth/refresh");

        // When
        boolean result = filter.shouldNotFilter(request);

        // Then
        assertThat(result).isTrue();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Scenario 3: /api/v1/auth/logout → shouldNotFilter() returns true
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should not filter logout path /api/v1/auth/logout")
    void shouldNotFilterLogoutPath() throws Exception {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/auth/logout");

        // When
        boolean result = filter.shouldNotFilter(request);

        // Then
        assertThat(result).isTrue();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Scenario 4: /api/v1/users/me → shouldNotFilter() returns false
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should filter protected path /api/v1/users/me")
    void shouldFilterProtectedPath() throws Exception {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/users/me");

        // When
        boolean result = filter.shouldNotFilter(request);

        // Then
        assertThat(result).isFalse();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Scenario 5: /actuator/health → shouldNotFilter() returns true (regression)
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should not filter actuator health path /actuator/health (regression guard)")
    void shouldNotFilterHealthPath() throws Exception {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/actuator/health");

        // When
        boolean result = filter.shouldNotFilter(request);

        // Then
        assertThat(result).isTrue();
    }
}
