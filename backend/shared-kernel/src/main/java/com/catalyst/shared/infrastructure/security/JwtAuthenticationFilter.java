package com.catalyst.shared.infrastructure.security;

import com.catalyst.shared.application.ports.JwtTokenPort;
import com.catalyst.shared.domain.auth.JwtClaims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.Nonnull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * Filter that extracts JWT tokens from requests and sets up Spring Security authentication.
 * 
 * <p>This filter:
 * <ul>
 *   <li>Extracts JWT from Authorization header (Bearer token)</li>
 *   <li>Validates the token signature and expiration</li>
 *   <li>Sets up SecurityContext with user authentication</li>
 * </ul>
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    
    private final JwtTokenPort jwtTokenPort;
    
    public JwtAuthenticationFilter(JwtTokenPort jwtTokenPort) {
        this.jwtTokenPort = jwtTokenPort;
    }
    
    @Override
    protected void doFilterInternal(
            @Nonnull HttpServletRequest request,
            @Nonnull HttpServletResponse response,
            @Nonnull FilterChain filterChain) throws ServletException, IOException {
        
        try {
            extractToken(request)
                .flatMap(jwtTokenPort::validateAccessToken)
                .ifPresent(claims -> setAuthentication(claims, request));
        } catch (Exception e) {
            log.error("Failed to process JWT authentication", e);
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * Extracts the JWT token from the Authorization header.
     */
    private Optional<String> extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        
        if (StringUtils.hasText(authHeader) && authHeader.startsWith(BEARER_PREFIX)) {
            String token = authHeader.substring(BEARER_PREFIX.length());
            return Optional.of(token);
        }
        
        return Optional.empty();
    }
    
    /**
     * Sets up Spring Security authentication from JWT claims.
     */
    private void setAuthentication(JwtClaims claims, HttpServletRequest request) {
        var authorities = claims.roles().stream()
            .map(SimpleGrantedAuthority::new)
            .toList();
        
        var userPrincipal = new UserPrincipal(
            claims.userId(),
            claims.email(),
            claims.roles()
        );
        
        var authentication = new UsernamePasswordAuthenticationToken(
            userPrincipal,
            null,
            authorities
        );
        
        authentication.setDetails(
            new WebAuthenticationDetailsSource().buildDetails(request)
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        log.debug("Authenticated user: {} with roles: {}", 
            claims.userId(), claims.roles());
    }
    
    @Override
    protected boolean shouldNotFilter(@Nonnull HttpServletRequest request) {
        String path = request.getServletPath();
        // Skip filtering for public endpoints
        return path.startsWith("/auth/") || 
               path.startsWith("/public/") ||
               path.startsWith("/actuator/health") ||
               path.startsWith("/error");
    }
}

