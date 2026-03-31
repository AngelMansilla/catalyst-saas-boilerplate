package com.catalyst.user.integration;

import com.catalyst.shared.BaseIntegrationTest;
import com.catalyst.shared.domain.auth.LoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the logout endpoint (T-20).
 *
 * <p>Covers:
 * <ol>
 *   <li>Login then logout → 204</li>
 *   <li>After logout, refresh with same token → 401</li>
 *   <li>Double logout with same token → 204 (idempotent)</li>
 * </ol>
 */
@AutoConfigureMockMvc
@Testcontainers
@DisplayName("Auth Logout Integration Tests (T-20)")
@TestPropertySource(properties = {
    "catalyst.security.jwt.secret=test-secret-key-for-jwt-signing-must-be-at-least-32-characters-long",
    "catalyst.security.jwt.access-token-expiration=15m",
    "catalyst.security.jwt.refresh-token-expiration=7d",
    "catalyst.security.jwt.issuer=catalyst-user-service-test"
})
class AuthLogoutIT extends BaseIntegrationTest {

    @Container
    static final GenericContainer<?> redis = new GenericContainer<>(
            DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProps(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String PASSWORD = "Password1!";

    // -------------------------------------------------------------------
    // Helper: register + login, return the refreshToken string
    // -------------------------------------------------------------------
    private String registerAndLogin(String email) throws Exception {
        String registerBody = String.format(
                "{\"email\":\"%s\",\"password\":\"%s\",\"name\":\"Logout Test User\"}", email, PASSWORD);
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerBody))
                .andExpect(status().isCreated());

        LoginRequest login = LoginRequest.of(email, PASSWORD);
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("refreshToken").asText();
    }

    // -------------------------------------------------------------------
    // Scenario 1 — login then logout → 204
    // -------------------------------------------------------------------
    @Test
    @DisplayName("S1: logout with valid refresh token should return 204")
    void shouldLogoutSuccessfully() throws Exception {
        // Given — a logged-in user
        String refreshToken = registerAndLogin("logout_s1@example.com");

        // When / Then
        mockMvc.perform(post("/api/v1/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("{\"refreshToken\":\"%s\"}", refreshToken)))
                .andExpect(status().isNoContent());
    }

    // -------------------------------------------------------------------
    // Scenario 2 — after logout, refresh with same token → 401
    // -------------------------------------------------------------------
    @Test
    @DisplayName("S2: refreshing after logout should return 401")
    void shouldReturn401OnRefreshAfterLogout() throws Exception {
        // Given — a logged-in user who then logs out
        String refreshToken = registerAndLogin("logout_s2@example.com");

        mockMvc.perform(post("/api/v1/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("{\"refreshToken\":\"%s\"}", refreshToken)))
                .andExpect(status().isNoContent());

        // When — try to use the invalidated token for a refresh
        // Then — 401 (token no longer exists in Redis)
        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("{\"refreshToken\":\"%s\"}", refreshToken)))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------
    // Scenario 3 — double logout with same token → 204 (idempotent)
    // -------------------------------------------------------------------
    @Test
    @DisplayName("S3: second logout with same token should still return 204 (idempotent)")
    void shouldBeIdempotentOnDoubleLogout() throws Exception {
        // Given — a logged-in user
        String refreshToken = registerAndLogin("logout_s3@example.com");
        String body = String.format("{\"refreshToken\":\"%s\"}", refreshToken);

        // First logout
        mockMvc.perform(post("/api/v1/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isNoContent());

        // When — second logout with the same (now-invalid) token
        // Then — still 204 (logout is idempotent by design)
        mockMvc.perform(post("/api/v1/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isNoContent());
    }
}
