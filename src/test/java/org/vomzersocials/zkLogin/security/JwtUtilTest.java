//package org.vomzersocials.zkLogin.security;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.TestPropertySource;
//import org.springframework.test.util.ReflectionTestUtils;
//import org.vomzersocials.user.springSecurity.JwtUtil;
//
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//@TestPropertySource(properties = {
//        "jwt.secret=0123456789abcdef0123456789abcdef",
//        "jwt.expiration-ms=60000",
//        "jwt.refresh-expiration-ms=120000"
//})
//public class JwtUtilTest {
//
//    @Autowired
//    JwtUtil jwtUtil;
//
//    @Test
//    public void generateToken_test() {
//        assertNotNull(jwtUtil.generateAccessToken("user123"));
//    }
//
//    @Test
//    public void validateAndExtractUsername_test() {
//        String tok = jwtUtil.generateAccessToken("alice");
//        assertTrue(jwtUtil.validateToken(tok));
//        assertEquals("alice", jwtUtil.extractUsername(tok));
//    }
//
//    @Test
//    public void expiredTokenIsInvalid_test() {
//        ReflectionTestUtils.setField(jwtUtil, "accessTokenExpirationMs", 1L);
//        jwtUtil.init();
//        String tok = jwtUtil.generateAccessToken("eve");
//        assertFalse(jwtUtil.validateToken(tok));
//    }
//
//    @Test
//    public void rolesClaim_roundTrip_test() {
//        List<String> roles = List.of("ADMIN", "SUBSCRIBER");
//        String tok = jwtUtil.generateAccessToken("bob", roles);
//        assertTrue(jwtUtil.validateToken(tok));
//        assertEquals("bob", jwtUtil.extractUsername(tok));
//        assertEquals(roles, jwtUtil.extractRoles(tok));
//    }
//
//    @Test
//    public void generateRefreshToken_test() {
//        String refreshToken = jwtUtil.generateRefreshToken("alice");
//        assertNotNull(refreshToken);
//        assertTrue(jwtUtil.validateToken(refreshToken));
//        assertEquals("alice", jwtUtil.extractUsername(refreshToken));
//    }
//
//    @Test
//    public void invalidToken_test() {
//        String invalidToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.invalid.signature";
//        assertFalse(jwtUtil.validateToken(invalidToken));
//        assertThrows(Exception.class, () -> jwtUtil.extractUsername(invalidToken));
//    }
//}