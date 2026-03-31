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
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the login endpoint (T-18).
 *
 * <p>Covers:
 * <ol>
 *   <li>Valid credentials → 200 + accessToken + refreshToken + user.email</li>
 *   <li>Wrong password → 401</li>
 *   <li>Unknown email → 401</li>
 *   <li>Missing email field → 400</li>
 *   <li>Invalid email format → 400</li>
 * </ol>
 */
@AutoConfigureMockMvc
@Testcontainers
@DisplayName("Auth Login Integration Tests (T-18)")
@TestPropertySource(properties = {
    "catalyst.security.jwt.secret=test-secret-key-for-jwt-signing-must-be-at-least-32-characters-long",
    "catalyst.security.jwt.access-token-expiration=15m",
    "catalyst.security.jwt.refresh-token-expiration=7d",
    "catalyst.security.jwt.issuer=catalyst-user-service-test"
})
class AuthLoginIT extends BaseIntegrationTest {

    // Redis container — refresh token storage requires Redis
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

    // -------------------------------------------------------------------
    // Helper: register a user so we can log in as them
    // -------------------------------------------------------------------
    private static final String PASSWORD = "Password1!";

    private void registerUser(String email) throws Exception {
        String body = String.format(
                "{\"email\":\"%s\",\"password\":\"%s\",\"name\":\"Login Test User\"}", email, PASSWORD);
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated());
    }

    // -------------------------------------------------------------------
    // Scenario 1 — valid credentials → 200 + tokens + user.email
    // -------------------------------------------------------------------
    @Test
    @DisplayName("S1: valid credentials should return 200 with accessToken, refreshToken, and user.email")
    void shouldReturnTokensOnValidLogin() throws Exception {
        // Given
        String email = "login_s1@example.com";
        registerUser(email);

        LoginRequest request = LoginRequest.of(email, PASSWORD);

        // When / Then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.user.email").value(email));
    }

    // -------------------------------------------------------------------
    // Scenario 2 — wrong password → 401
    // -------------------------------------------------------------------
    @Test
    @DisplayName("S2: wrong password should return 401")
    void shouldReturn401OnWrongPassword() throws Exception {
        // Given
        String email = "login_s2@example.com";
        registerUser(email);

        LoginRequest request = LoginRequest.of(email, "wrongPassword99");

        // When / Then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------
    // Scenario 3 — unknown email → 401
    // -------------------------------------------------------------------
    @Test
    @DisplayName("S3: unknown email should return 401")
    void shouldReturn401OnUnknownEmail() throws Exception {
        // Given — no user registered with this email
        LoginRequest request = LoginRequest.of("nobody@unknown.com", PASSWORD);

        // When / Then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------
    // Scenario 4 — missing email field → 400
    // -------------------------------------------------------------------
    @Test
    @DisplayName("S4: missing email field should return 400")
    void shouldReturn400WhenEmailIsMissing() throws Exception {
        // Given — body without the email field
        String body = "{\"password\":\"password123\",\"rememberMe\":false}";

        // When / Then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------
    // Scenario 5 — invalid email format → 400
    // -------------------------------------------------------------------
    @Test
    @DisplayName("S5: invalid email format should return 400")
    void shouldReturn400WhenEmailFormatIsInvalid() throws Exception {
        // Given — body with malformed email address
        String body = "{\"email\":\"not-an-email\",\"password\":\"password123\",\"rememberMe\":false}";

        // When / Then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest());
    }
}
