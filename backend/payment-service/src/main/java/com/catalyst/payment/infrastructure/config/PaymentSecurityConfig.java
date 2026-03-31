package com.catalyst.payment.infrastructure.config;

import com.catalyst.shared.infrastructure.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration for the Payment Service.
 * Allows public access to certain endpoints for testing and development.
 */
@Configuration
@EnableWebSecurity
public class PaymentSecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public PaymentSecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain paymentSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/v1/payments/**", "/api/v1/webhooks/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Checkout requires authentication; webhook stays public (Stripe cannot send JWT)
                        .requestMatchers("/api/v1/payments/subscriptions/checkout").authenticated()
                        .requestMatchers("/api/v1/webhooks/stripe").permitAll()
                        // Other payment endpoints might still need authentication in a real scenario
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
