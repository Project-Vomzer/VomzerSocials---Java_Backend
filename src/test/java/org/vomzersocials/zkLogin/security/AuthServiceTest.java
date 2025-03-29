//package org.vomzersocials.zkLogin.security;
//
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.vomzersocials.user.data.repositories.UserRepository;
//import org.vomzersocials.zkLogin.dtos.ZkLoginRequest;
//
//import javax.naming.AuthenticationException;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.when;
//
//@SpringBootTest
//@ExtendWith(MockitoExtension.class)
//public class AuthServiceTest {
//    @Mock
//    private UserRepository userRepository;
//    @Mock
//    private ZkLoginVerifier zkLoginVerifier;
//    @InjectMocks
//    private AuthService authService;
//    private JwtUtil jwtUtil = new JwtUtil();
//    private ZkLoginRequest zkLoginRequest;
//
//    @BeforeEach
//    public void setUp() {
//
//        String zkProof = "valid_proof";
//        String publicKey = "user_public_key";
//
////        zkLoginRequest = new ZkLoginRequest();
//
//        zkLoginRequest.setPublicKey(publicKey);
//        zkLoginRequest.setZkProof(zkProof);
//
//    }
//
//    @Test
//    void testAuthenticateUser_Success() throws javax.naming.AuthenticationException {
//        String zkProof = "valid_proof";
//        String publicKey = "user_public_key";
//
//        when(zkLoginVerifier.verifyProof(zkProof, publicKey)).thenReturn(true);
//        when(userRepository.findByPublicKey(publicKey)).thenReturn(Optional.empty());
//
//        String jwt = authService.authenticateUser(zkLoginRequest);
//
//        assertNotNull(jwt);
//    }
//
//    @Test
//    void testAuthenticateUser_InvalidProof() {
//        String zkProof = "invalid_proof";
//        String publicKey = "user_public_key";
//
//        when(zkLoginVerifier.verifyProof(zkProof, publicKey)).thenReturn(false);
//
//        assertThrows(AuthenticationException.class, () -> authService.authenticateUser(zkLoginRequest));
//    }
//}