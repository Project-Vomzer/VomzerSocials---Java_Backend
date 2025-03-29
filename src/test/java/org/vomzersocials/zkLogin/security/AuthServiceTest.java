package org.vomzersocials.zkLogin.security;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.vomzersocials.user.data.models.User;
import org.vomzersocials.user.data.repositories.UserRepository;
import org.vomzersocials.zkLogin.dtos.ZkLoginRequest;
import org.vomzersocials.zkLogin.security.AuthService;
import org.vomzersocials.zkLogin.security.ZkLoginVerifier;

import javax.naming.AuthenticationException;
import java.util.Optional;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ZkLoginVerifier zkLoginVerifier;

    @InjectMocks
    private AuthService authService;

    private ZkLoginRequest zkLoginRequest;
    private String zkProof;
    private String publicKey;

    @BeforeEach
    public void setUp() {
        zkProof = "valid_proof";
        publicKey = "user_public_key";

        zkLoginRequest = new ZkLoginRequest();
        zkLoginRequest.setPublicKey(publicKey);
        zkLoginRequest.setZkProof(zkProof);
    }

    @Test
    void testAuthenticateUser_Success() throws AuthenticationException {
        // Create a mock user with an ID
        User mockUser = new User();
        mockUser.setPublicKey(publicKey);
        mockUser.setId(String.valueOf(1L));

        // Stub non-static methods with proper matchers
        when(zkLoginVerifier.verifyProof(any(String.class), any(String.class))).thenReturn(true);
        when(userRepository.findByPublicKey(any(String.class))).thenReturn(Optional.of(mockUser));

        // Use static mocking for JwtUtil.generateAccessToken
        try (var jwtUtilMock = mockStatic(JwtUtil.class)) {
            jwtUtilMock.when(() -> JwtUtil.generateAccessToken(anyString()))
                    .thenReturn("mocked_jwt_token");

            // Call the method under test
            String jwt = authService.authenticateUser(zkLoginRequest);

            // Verify the outcome
            assertNotNull(jwt);
            assertEquals("mocked_jwt_token", jwt);
        }
    }

    @Test
    void testAuthenticateUser_InvalidProof() {

        // Expect an AuthenticationException when proof is invalid
        assertThrows(AuthenticationException.class, () -> authService.authenticateUser(zkLoginRequest));
    }
}
