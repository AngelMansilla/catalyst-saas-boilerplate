package com.catalyst.payment.integration;

import com.catalyst.shared.BaseIntegrationTest;
import com.catalyst.shared.infrastructure.security.JwtTokenService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests verifying JWT authentication enforcement on payment endpoints.
 *
 * <p>Key invariants:
 * <ul>
 *   <li>{@code /api/v1/payments/subscriptions/checkout} requires a valid JWT (401 without it)</li>
 *   <li>{@code /api/v1/webhooks/stripe} is public (Stripe cannot send JWTs)</li>
 * </ul>
 */
@AutoConfigureMockMvc
@DisplayName("Checkout Auth Integration Tests")
@TestPropertySource(properties = {
    "catalyst.security.jwt.secret=test-secret-key-for-jwt-signing-must-be-at-least-32-characters-long",
    "catalyst.security.jwt.access-token-expiration=15m",
    "catalyst.security.jwt.refresh-token-expiration=7d",
    "catalyst.security.jwt.issuer=catalyst-user-service"
})
class CheckoutAuthIT extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    private static final String CHECKOUT_URL = "/api/v1/payments/subscriptions/checkout";
    private static final String WEBHOOK_URL = "/api/v1/webhooks/stripe";

    /** Minimal valid-looking checkout body (real business validation may still fail — that's OK). */
    private String checkoutBody;

    @BeforeEach
    void setUp() {
        checkoutBody = """
                {
                  "userId": "%s",
                  "email": "user@example.com",
                  "name": "Test User",
                  "tier": "PROFESSIONAL",
                  "billingCycle": "MONTHLY",
                  "successUrl": "http://localhost:3000/success",
                  "cancelUrl": "http://localhost:3000/cancel"
                }
                """.formatted(UUID.randomUUID());
    }

    // -------------------------------------------------------------------
    // T-21 Scenario 1: checkout without JWT → 401
    // -------------------------------------------------------------------
    @Test
    @DisplayName("Should return 401 when checkout request has no JWT")
    void shouldReturn401CheckoutWithoutJwt() throws Exception {
        // Given – no Authorization header
        // When / Then
        mockMvc.perform(post(CHECKOUT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(checkoutBody))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------
    // T-21 Scenario 2: checkout with invalid JWT → 401
    // -------------------------------------------------------------------
    @Test
    @DisplayName("Should return 401 when checkout request has invalid JWT")
    void shouldReturn401CheckoutWithInvalidJwt() throws Exception {
        // Given – a garbage token
        String invalidToken = "Bearer this.is.not.a.valid.jwt.token";

        // When / Then
        mockMvc.perform(post(CHECKOUT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, invalidToken)
                .content(checkoutBody))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------
    // T-21 Scenario 3: checkout with valid JWT → not 401
    //   (200 or business error like 400/422 is acceptable — testing auth only)
    // -------------------------------------------------------------------
    @Test
    @DisplayName("Should not return 401 when checkout request has a valid JWT")
    void shouldNotReturn401CheckoutWithValidJwt() throws Exception {
        // Given – a valid JWT generated directly from JwtTokenService
        UUID userId = UUID.randomUUID();
        String accessToken = jwtTokenService.generateAccessToken(
                userId, "user@example.com", Set.of("ROLE_USER"));

        // When
        int status = mockMvc.perform(post(CHECKOUT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .content(checkoutBody))
                .andReturn()
                .getResponse()
                .getStatus();

        // Then – must not be 401 (auth must pass; business errors are fine)
        Assertions.assertThat(status).isNotEqualTo(401);
    }

    // -------------------------------------------------------------------
    // T-21 Scenario 4: webhook stays public (no JWT needed → not 401)
    // -------------------------------------------------------------------
    @Test
    @DisplayName("Should not return 401 for webhook endpoint (public endpoint, no JWT required)")
    void shouldNotReturn401ForWebhookWithoutJwt() throws Exception {
        // Given – a raw Stripe event payload without JWT
        String webhookPayload = "{\"id\":\"evt_test\",\"type\":\"checkout.session.completed\"}";

        // When
        int status = mockMvc.perform(post(WEBHOOK_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(webhookPayload))
                .andReturn()
                .getResponse()
                .getStatus();

        // Then – must not be 401 (webhook is public; Stripe signature missing gives 400 — that's fine)
        Assertions.assertThat(status).isNotEqualTo(401);
    }
}
