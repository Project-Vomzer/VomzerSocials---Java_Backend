//package org.vomzersocials.zkLogin.security;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.vomzersocials.user.data.models.User;
//import org.vomzersocials.user.springSecurity.JwtUtil;
//
//import static org.junit.jupiter.api.Assertions.*;
//@SpringBootTest
//public class JwtUtilTest {
//
//    @Test
//    void testGenerateToken() {
//        User user = new User();
//        user.setPublicKey("user_public_key");
//
//        String token = JwtUtil.generateAccessToken(user.getId());
//
//        assertNotNull(token);
//    }
//}
