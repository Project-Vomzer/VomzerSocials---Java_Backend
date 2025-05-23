package org.vomzersocials.zkLogin.security;//package org.vomzersocials.zkLogin.security;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.util.ReflectionTestUtils;
//import org.vomzersocials.user.springSecurity.JwtUtil;
//
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest(
//        classes = JwtUtil.class,
//        properties = {
//                "jwt.secret=0123456789abcdef0123456789abcdef",
//                "jwt.expiration-ms=60000",
//                "jwt.refresh-expiration-ms=120000"
//        }
//)
//public class JwtUtilTest {
//
//    @Autowired
//    JwtUtil jwtUtil;
//
//    @BeforeEach
//    public void ensureKeyBuilt() {
//        jwtUtil.init();
//    }
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
//    public void expiredTokenIsInvalid_test() throws InterruptedException {
//        ReflectionTestUtils.setField(jwtUtil, "accessTokenExpirationMs", 1L);
//        jwtUtil.init();
//        String tok = jwtUtil.generateAccessToken("eve");
//        Thread.sleep(5);
//        assertFalse(jwtUtil.validateToken(tok));
//    }
//
//    @Test
//    public void rolesClaim_roundTrip_test() {
//        // given
//        List<String> roles = List.of("ADMIN", "SUBSCRIBER");
//        // when
//        String tok = jwtUtil.generateAccessToken("bob", roles);
//        // then
//        assertTrue(jwtUtil.validateToken(tok));
//        assertEquals("bob", jwtUtil.extractUsername(tok));
//    }
//
//}
