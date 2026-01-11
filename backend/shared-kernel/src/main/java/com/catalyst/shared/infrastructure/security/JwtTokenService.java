package com.catalyst.shared.infrastructure.security;

import com.catalyst.shared.application.ports.JwtTokenPort;
import com.catalyst.shared.domain.auth.JwtClaims;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;

/**
 * Implementation of JwtTokenPort for JWT token operations.
 * Uses HS256 algorithm for access tokens and opaque tokens for refresh.
 */
@Service
public class JwtTokenService implements JwtTokenPort {
    
    private static final Logger log = LoggerFactory.getLogger(JwtTokenService.class);
    
    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_TOKEN_ID = "jti";
    
    private final JwtProperties properties;
    private final SecretKey signingKey;
    private final SecureRandom secureRandom;
    
    public JwtTokenService(JwtProperties properties) {
        this.properties = properties;
        this.signingKey = Keys.hmacShaKeyFor(
            properties.getSecret().getBytes(StandardCharsets.UTF_8)
        );
        this.secureRandom = new SecureRandom();
    }
    
    @Override
    public String generateAccessToken(UUID userId, String email, Set<String> roles) {
        return generateAccessToken(userId, email, roles, Map.of());
    }
    
    @Override
    public String generateAccessToken(
            UUID userId, 
            String email, 
            Set<String> roles,
            Map<String, Object> additionalClaims) {
        
        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(properties.getAccessTokenExpirationSeconds());
        String tokenId = UUID.randomUUID().toString();
        
        var claimsBuilder = Jwts.claims()
            .subject(userId.toString())
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiration))
            .issuer(properties.getIssuer())
            .audience().add(properties.getAudience()).and()
            .add(CLAIM_TOKEN_ID, tokenId)
            .add(CLAIM_EMAIL, email)
            .add(CLAIM_ROLES, new ArrayList<>(roles));
        
        // Add any additional claims
        additionalClaims.forEach(claimsBuilder::add);
        
        String token = Jwts.builder()
            .claims(claimsBuilder.build())
            .signWith(signingKey, Jwts.SIG.HS256)
            .compact();
        
        log.debug("Generated access token for user: {}, expires: {}", userId, expiration);
        return token;
    }
    
    @Override
    public String generateRefreshToken() {
        byte[] randomBytes = new byte[properties.getRefreshTokenLength()];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
    
    @Override
    public Optional<JwtClaims> validateAccessToken(String token) {
        try {
            Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(properties.getIssuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
            
            return Optional.of(mapToJwtClaims(claims));
            
        } catch (ExpiredJwtException e) {
            log.debug("Token expired: {}", e.getMessage());
            return Optional.empty();
        } catch (JwtException e) {
            log.warn("Invalid token: {}", e.getMessage());
            return Optional.empty();
        }
    }
    
    @Override
    public Optional<JwtClaims> extractClaims(String token) {
        try {
            // Parse without validation - use with caution
            Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
            
            return Optional.of(mapToJwtClaims(claims));
            
        } catch (ExpiredJwtException e) {
            // Still return claims even if expired
            return Optional.of(mapToJwtClaims(e.getClaims()));
        } catch (JwtException e) {
            log.debug("Failed to extract claims: {}", e.getMessage());
            return Optional.empty();
        }
    }
    
    @Override
    public boolean isTokenExpired(String token) {
        try {
            Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token);
            return false;
        } catch (ExpiredJwtException e) {
            return true;
        } catch (JwtException e) {
            return true;
        }
    }
    
    @Override
    public Optional<UUID> extractUserId(String token) {
        return extractClaims(token)
            .map(JwtClaims::userId);
    }
    
    @Override
    public long getAccessTokenExpirationSeconds() {
        return properties.getAccessTokenExpirationSeconds();
    }
    
    @Override
    public long getRefreshTokenExpirationSeconds() {
        return properties.getRefreshTokenExpirationSeconds();
    }
    
    private JwtClaims mapToJwtClaims(Claims claims) {
        UUID userId = UUID.fromString(claims.getSubject());
        String email = claims.get(CLAIM_EMAIL, String.class);
        
        @SuppressWarnings("unchecked")
        List<String> rolesList = claims.get(CLAIM_ROLES, List.class);
        Set<String> roles = rolesList != null ? new HashSet<>(rolesList) : Set.of();
        
        String tokenId = claims.get(CLAIM_TOKEN_ID, String.class);
        Instant issuedAt = claims.getIssuedAt().toInstant();
        Instant expiresAt = claims.getExpiration().toInstant();
        
        // Extract additional claims (excluding standard ones)
        Map<String, Object> additionalClaims = new HashMap<>();
        claims.forEach((key, value) -> {
            if (!isStandardClaim(key)) {
                additionalClaims.put(key, value);
            }
        });
        
        return new JwtClaims(
            userId, email, roles, issuedAt, 
            expiresAt, tokenId, additionalClaims
        );
    }
    
    private boolean isStandardClaim(String key) {
        return Set.of(
            "sub", "iat", "exp", "iss", "aud",
            CLAIM_EMAIL, CLAIM_ROLES, CLAIM_TOKEN_ID
        ).contains(key);
    }
}

