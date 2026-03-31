package com.catalyst.user.integration;

import com.catalyst.shared.BaseIntegrationTest;
import com.catalyst.shared.domain.auth.LoginRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
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

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the token refresh endpoint (T-19).
 *
 * <p>Covers:
 * <ol>
 *   <li>Valid refresh token → 200 + new tokens, new refreshToken differs from old</li>
 *   <li>Invalid (random UUID) refresh token → 401</li>
 *   <li>Token rotation: refresh T1 → T2, then use T1 again → 401</li>
 *   <li>Blank refresh token → 400</li>
 * </ol>
 */
@AutoConfigureMockMvc
@Testcontainers
@DisplayName("Auth Refresh Integration Tests (T-19)")
@TestPropertySource(properties = {
    "catalyst.security.jwt.secret=test-secret-key-for-jwt-signing-must-be-at-least-32-characters-long",
    "catalyst.security.jwt.access-token-expiration=15m",
    "catalyst.security.jwt.refresh-token-expiration=7d",
    "catalyst.security.jwt.issuer=catalyst-user-service-test"
})
class AuthRefreshIT extends BaseIntegrationTest {

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
    // Helper: register + login, return the full login response as JsonNode
    // -------------------------------------------------------------------
    private JsonNode registerAndLogin(String email) throws Exception {
        String registerBody = String.format(
                "{\"email\":\"%s\",\"password\":\"%s\",\"name\":\"Refresh Test User\"}", email, PASSWORD);
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

        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    // -------------------------------------------------------------------
    // Scenario 1 — valid refresh token → 200 + new tokens, new RT differs
    // -------------------------------------------------------------------
    @Test
    @DisplayName("S1: valid refresh token should return 200 with new token pair")
    void shouldRefreshWithValidToken() throws Exception {
        // Given
        JsonNode loginResp = registerAndLogin("refresh_s1@example.com");
        String originalRefreshToken = loginResp.get("refreshToken").asText();

        String body = String.format("{\"refreshToken\":\"%s\"}", originalRefreshToken);

        // When
        MvcResult refreshResult = mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn();

        // Then — new refreshToken must differ from the original
        JsonNode refreshResp = objectMapper.readTree(refreshResult.getResponse().getContentAsString());
        String newRefreshToken = refreshResp.get("refreshToken").asText();
        Assertions.assertThat(newRefreshToken).isNotEqualTo(originalRefreshToken);
    }

    // -------------------------------------------------------------------
    // Scenario 2 — invalid (random UUID) refresh token → 401
    // -------------------------------------------------------------------
    @Test
    @DisplayName("S2: invalid refresh token should return 401")
    void shouldReturn401WhenRefreshTokenIsInvalid() throws Exception {
        // Given — a token that was never issued
        String fakeToken = UUID.randomUUID().toString();
        String body = String.format("{\"refreshToken\":\"%s\"}", fakeToken);

        // When / Then
        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------
    // Scenario 3 — token rotation: refresh T1 → T2, then use T1 again → 401
    // -------------------------------------------------------------------
    @Test
    @DisplayName("S3: original token should be invalid after rotation (token rotation)")
    void shouldRejectOriginalTokenAfterRotation() throws Exception {
        // Given — login to get T1
        JsonNode loginResp = registerAndLogin("refresh_s3@example.com");
        String t1 = loginResp.get("refreshToken").asText();

        // Refresh with T1 → receive T2
        String bodyT1 = String.format("{\"refreshToken\":\"%s\"}", t1);
        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bodyT1))
                .andExpect(status().isOk());

        // When — try to use T1 again
        // Then — 401 (T1 was deleted during rotation)
        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bodyT1))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------
    // Scenario 4 — blank refresh token → 400
    // -------------------------------------------------------------------
    @Test
    @DisplayName("S4: blank refresh token should return 400")
    void shouldReturn400WhenRefreshTokenIsBlank() throws Exception {
        // Given — body with empty string token (fails @NotBlank validation)
        String body = "{\"refreshToken\":\"\"}";

        // When / Then
        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest());
    }
}
