package com.catalyst.shared.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Shared kernel configuration.
 * Provides common beans used across all services.
 */
@Configuration
public class SharedKernelConfig {

    /**
     * Configures ObjectMapper with Java 8 time module.
     * This bean is used by shared components like RateLimitFilter.
     *
     * @return configured ObjectMapper
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
}
