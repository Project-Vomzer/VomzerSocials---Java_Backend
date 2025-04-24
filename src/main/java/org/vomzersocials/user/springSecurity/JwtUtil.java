package org.vomzersocials.user.springSecurity;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.issuer}")
    private String issuer;

    public String generateAccessToken(String id) {
        return JWT.create()
                .withClaim("user_id", id)
                .withIssuer(issuer)
                .sign(Algorithm.HMAC256(jwtSecret));
    }
}
