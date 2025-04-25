package org.vomzersocials.user.springSecurity;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private static String jwtSecret;

    @Value("${jwt.issuer}")
    private static String issuer;

    public static String generateAccessToken(String id) {
        return JWT.create()
                .withClaim("user_id", id)
                .withIssuer(issuer)
                .sign(Algorithm.HMAC256(jwtSecret));
    }

    public String extractUserId(String token) {
        return JWT.decode(token).getClaim("user_id").asString();
    }

    public boolean validateToken(String token) {
        return JWT.decode(token).getClaim("user_id") != null;
    }
}
