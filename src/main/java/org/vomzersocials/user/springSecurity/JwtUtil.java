package org.vomzersocials.user.springSecurity;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration-ms}")
    private long accessTokenExpirationMs;

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshTokenExpirationMs;

    private Key signingKey;

    @PostConstruct
    public void init() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) throw new IllegalArgumentException("Secret key must be at least 256 bits (32 bytes)");
        signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

//    public String generateAccessToken(String username, List<String> roles) {
//        Map<String, Object> claims = new HashMap<>();
//        claims.put("roles", roles);
//        return Jwts.builder()
//                .setSubject(username)
//                .addClaims(claims)
//                .setIssuedAt(new Date())
//                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpirationMs))
//                .signWith(signingKey, SignatureAlgorithm.HS256)
//                .compact();
//    }

    public String generateAccessToken(String username, List<String> roles) {
        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpirationMs))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

//    no role tokens for tests
    public String generateAccessToken(String username) {
        return generateAccessToken(username, List.of());
    }

    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpirationMs))
                .signWith(signingKey)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException ex) {
            return false;
        }
    }

    public String extractUsername(String token) {
//        Jws<Claims> jws = Jwts.parserBuilder()
//                .setSigningKey(signingKey)
//                .build()
//                .parseClaimsJws(token);
//        return jws.getBody().getSubject();
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build().parseClaimsJws(token)
                .getBody().getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        var body = Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        Object roles = body.get("roles");
        if (roles instanceof List<?>) {
            return (List<String>) roles;
        }
        return List.of();
    }



}
