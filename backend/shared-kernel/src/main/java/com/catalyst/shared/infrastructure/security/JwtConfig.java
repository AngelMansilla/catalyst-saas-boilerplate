package com.catalyst.shared.infrastructure.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for JWT-related beans.
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {
    
    /**
     * Creates the JWT token service.
     * Note: This bean may be created automatically via @Service annotation,
     * but explicit configuration allows for more control and testing.
     */
    @Bean
    public JwtTokenService jwtTokenService(JwtProperties jwtProperties) {
        return new JwtTokenService(jwtProperties);
    }
}

