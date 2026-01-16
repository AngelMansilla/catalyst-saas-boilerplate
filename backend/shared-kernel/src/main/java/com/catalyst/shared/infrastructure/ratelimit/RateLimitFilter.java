package com.catalyst.shared.infrastructure.ratelimit;

import com.catalyst.shared.application.dto.ApiError;
import com.catalyst.shared.infrastructure.security.UserPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import jakarta.annotation.Nonnull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

/**
 * Filter that applies rate limiting to incoming requests.
 * Runs before authentication to also limit unauthenticated requests.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitFilter extends OncePerRequestFilter {
    
    
    private static final String HEADER_LIMIT = "X-RateLimit-Limit";
    private static final String HEADER_REMAINING = "X-RateLimit-Remaining";
    private static final String HEADER_RESET = "X-RateLimit-Reset";
    private static final String HEADER_RETRY_AFTER = "Retry-After";
    
    private final RateLimitService rateLimitService;
    private final RateLimitProperties properties;
    private final RateLimitKeyExtractor keyExtractor;
    private final ObjectMapper objectMapper;
    
    public RateLimitFilter(
            RateLimitService rateLimitService,
            RateLimitProperties properties,
            RateLimitKeyExtractor keyExtractor,
            ObjectMapper objectMapper) {
        this.rateLimitService = rateLimitService;
        this.properties = properties;
        this.keyExtractor = keyExtractor;
        this.objectMapper = objectMapper;
    }
    
    @Override
    protected void doFilterInternal(
            @Nonnull HttpServletRequest request,
            @Nonnull HttpServletResponse response,
            @Nonnull FilterChain filterChain) throws ServletException, IOException {
        
        if (!properties.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }
        
        String key = keyExtractor.extractKey(request);
        RateLimitTier tier = determineTier();
        
        RateLimitService.RateLimitResult result = rateLimitService.tryConsume(key, tier);
        
        // Add rate limit headers
        response.setHeader(HEADER_LIMIT, String.valueOf(result.limit()));
        response.setHeader(HEADER_REMAINING, String.valueOf(result.remaining()));
        response.setHeader(HEADER_RESET, String.valueOf(
            Instant.now().plusSeconds(result.retryAfterSeconds()).getEpochSecond()
        ));
        
        if (result.isAllowed()) {
            filterChain.doFilter(request, response);
        } else {
            response.setHeader(HEADER_RETRY_AFTER, String.valueOf(result.retryAfterSeconds()));
            sendRateLimitExceededResponse(request, response, result);
        }
    }
    
    private RateLimitTier determineTier() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null || !auth.isAuthenticated()) {
            return RateLimitTier.ANONYMOUS;
        }
        
        if (auth.getPrincipal() instanceof UserPrincipal user) {
            // Check for premium role
            if (user.hasRole("ROLE_PREMIUM") || user.hasRole("ROLE_ADMIN")) {
                return RateLimitTier.PREMIUM;
            }
            return RateLimitTier.AUTHENTICATED;
        }
        
        return RateLimitTier.ANONYMOUS;
    }
    
    private void sendRateLimitExceededResponse(
            HttpServletRequest request,
            HttpServletResponse response,
            RateLimitService.RateLimitResult result) throws IOException {
        
        var apiError = ApiError.builder()
            .code("RATE_LIMIT.EXCEEDED")
            .message("Too many requests. Please try again later.")
            .status(HttpStatus.TOO_MANY_REQUESTS.value())
            .path(request.getRequestURI())
            .timestamp(Instant.now())
            .addDetail("retryAfterSeconds", result.retryAfterSeconds())
            .addDetail("limit", result.limit())
            .build();
        
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(apiError.toResponse()));
    }
    
    @Override
    protected boolean shouldNotFilter(@Nonnull HttpServletRequest request) {
        String path = request.getServletPath();
        // Skip rate limiting for health checks and static resources
        return path.startsWith("/actuator/health") ||
               path.startsWith("/actuator/info") ||
               path.startsWith("/static/") ||
               path.startsWith("/favicon");
    }
}

