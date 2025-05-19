package org.vomzersocials.user.springSecurity;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration-ms}")
    private long accessTokenExpirationMs;

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshTokenExpirationMs;

    private Key signingKey;
    private final SuiZkLoginClient suiZkLoginClient;

    public JwtUtil(SuiZkLoginClient suiZkLoginClient) {
        this.suiZkLoginClient = suiZkLoginClient;
    }

    @PostConstruct
    public void init() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("Secret key must be at least 256 bits (32 bytes)");
        }
        signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(String username, List<String> roles) {
        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpirationMs))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpirationMs))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            if (token == null || token.isBlank()) {
                log.warn("JWT token is empty");
                return false;
            }
            Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT token is expired: {}", e.getMessage());
            return false;
        } catch (JwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.warn("Unexpected error validating JWT: {}", e.getMessage());
            return false;
        }
    }

    public String extractUsername(String token) {
        try {
            Claims claims = parseClaims(token);
            String username = claims.getSubject();
            if (username == null || username.isBlank()) {
                throw new IllegalArgumentException("Username is empty");
            }
            return username;
        } catch (Exception e) {
            log.warn("Failed to extract username from JWT: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid token");
        }
    }

    public List<String> extractRoles(String token) {
        try {
            Claims claims = parseClaims(token);
            Object roles = claims.get("roles");
            if (roles instanceof List<?>) {
                return (List<String>) roles;
            }
            return List.of();
        } catch (Exception e) {
            log.warn("Failed to extract roles from JWT: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid token");
        }
    }

    private Claims parseClaims(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token is empty");
        }
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateZkLoginToken(String oauthToken) {
        try {
            if (oauthToken == null || oauthToken.isBlank()) {
                log.warn("zkLogin OAuth token is empty");
                return false;
            }
            // Placeholder: Replace with actual Sui zkLogin validation
            boolean isValid = suiZkLoginClient.validateZkLogin(oauthToken);
            if (isValid) {
                log.info("zkLogin token validated successfully");
                return true;
            } else {
                log.warn("Invalid zkLogin token");
                return false;
            }
        } catch (Exception e) {
            log.warn("Error validating zkLogin token: {}", e.getMessage());
            return false;
        }
    }

    public TokenResponse generateTokensForLogin(String username, List<String> roles) {
        String accessToken = generateAccessToken(username, roles);
        String refreshToken = generateRefreshToken(username);
        return new TokenResponse(accessToken, refreshToken);
    }

    public String refreshAccessToken(String refreshToken, List<String> roles) {
        if (!validateToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }
        String username = extractUsername(refreshToken);
        return generateAccessToken(username, roles);
    }

    public record TokenResponse(String accessToken, String refreshToken) {
    }

    // Mock interface for Sui zkLogin client
    public interface SuiZkLoginClient {
        boolean validateZkLogin(String oauthToken);
    }
}