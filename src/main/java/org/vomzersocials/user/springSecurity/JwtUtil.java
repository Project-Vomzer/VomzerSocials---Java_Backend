package org.vomzersocials.user.springSecurity;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.net.URL;
import java.security.PublicKey;
import java.security.Key;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class JwtUtil {
    @Value("${jwt.oauth.jwk-url:https://www.googleapis.com/oauth2/v3/certs}")
    private String jwkUrl;

    private final ConcurrentHashMap<String, PublicKey> publicKeys = new ConcurrentHashMap<>();

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration-ms}")
    private long accessTokenExpirationMs;

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshTokenExpirationMs;

    private Key signingKey;

    @PostConstruct
    public void initSymmetricKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("Secret key must be at least 256 bits (32 bytes)");
        }
        signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    @Scheduled(fixedRate = 86400000)
    public void refreshJwk() {
        try {
            JWKSet jwkSet = JWKSet.load(new URL(jwkUrl));
            ConcurrentHashMap<String, PublicKey> newKeys = new ConcurrentHashMap<>();
            jwkSet.getKeys().forEach(jwk -> {
                if (jwk instanceof RSAKey rsaKey) {
                    try {
                        newKeys.put(rsaKey.getKeyID(), rsaKey.toPublicKey());
                    } catch (JOSEException e) {
                        log.error("Failed to parse RSA key: {}", e.getMessage());
                    }
                }
            });
            publicKeys.clear();
            publicKeys.putAll(newKeys);
            log.info("Refreshed {} public keys from {}", publicKeys.size(), jwkUrl);
        } catch (Exception e) {
            log.error("Failed to refresh JWK: {}", e.getMessage(), e);
        }
    }

    @PostConstruct
    public void init() {
        try {
            JWKSet jwkSet = JWKSet.load(new URL(jwkUrl));
            jwkSet.getKeys().forEach(jwk -> {
                if (jwk instanceof RSAKey rsaKey) {
                    try {
                        publicKeys.put(rsaKey.getKeyID(), rsaKey.toPublicKey());
                    } catch (JOSEException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            log.info("Loaded {} public keys from {}", publicKeys.size(), jwkUrl);
        } catch (Exception e) {
            log.error("Failed to load JWK: {}", e.getMessage(), e);
            throw new IllegalStateException("Cannot initialize JwtUtil", e);
        }
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
            Jws<Claims> jws = Jwts.parserBuilder()
                    .setSigningKeyResolver(new SigningKeyResolverAdapter() {
                        @Override
                        public Key resolveSigningKey(JwsHeader header, Claims claims) {
                            String alg = header.getAlgorithm();
                            if ("HS256".equals(alg)) {
                                return signingKey;
                            }
                            String keyId = header.getKeyId();
                            PublicKey key = publicKeys.get(keyId);
                            if (key == null) {
                                log.warn("No public key found for kid: {}", keyId);
                            }
                            return key;
                        }
                    })
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
            return false;
        } catch (JwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKeyResolver(new SigningKeyResolverAdapter() {
                    @Override
                    public Key resolveSigningKey(JwsHeader header, Claims claims) {
                        String alg = header.getAlgorithm();
                        if ("HS256".equals(alg)) {
                            return signingKey;
                        }
                        String keyId = header.getKeyId();
                        PublicKey key = publicKeys.get(keyId);
                        if (key == null) {
                            log.warn("No public key found for kid: {}", keyId);
                        }
                        return key;
                    }
                })
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public List<String> extractRoles(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKeyResolver(new SigningKeyResolverAdapter() {
                    @Override
                    public Key resolveSigningKey(JwsHeader header, Claims claims) {
                        String alg = header.getAlgorithm();
                        if ("HS256".equals(alg)) {
                            return signingKey;
                        }
                        String keyId = header.getKeyId();
                        PublicKey key = publicKeys.get(keyId);
                        if (key == null) {
                            log.warn("No public key found for kid: {}", keyId);
                        }
                        return key;
                    }
                })
                .build()
                .parseClaimsJws(token)
                .getBody();
        Object roles = claims.get("roles");
        if (roles instanceof List<?>) {
            return (List<String>) roles;
        }
        return List.of();
    }
}