package org.vomzersocials.zkLogin.security;
//
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.SignatureAlgorithm;
//import org.springframework.stereotype.Component;
//import org.vomzersocials.user.data.models.User;
//
//import java.util.Date;
//
//import static javax.crypto.Cipher.SECRET_KEY;
//
//@Component
//public class JwtUtil {
//
//    private static final String JWT_SECRET = "VomzerSocials";
//
//    public String generateToken(User user) {
//        return Jwts.builder()
//                .setSubject(user.getPublicKey())
//                .setIssuedAt(new Date())
//                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))  // 1 hour expiry
//                .signWith(SignatureAlgorithm.HS256, String.valueOf(SECRET_KEY))
//                .compact();
//    }
//
//}

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {


    public static String generateAccessToken(String id){
        return JWT.create()
                .withClaim("user_id", id)
                .withIssuer("admin_db")
                .sign(Algorithm.HMAC256("secret"));
    }

}