package org.vomzersocials.zkLogin.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vomzersocials.user.data.models.User;
import org.vomzersocials.user.data.repositories.UserRepository;
import org.vomzersocials.zkLogin.dtos.ZkLoginRequest;

import javax.naming.AuthenticationException;
import java.util.Optional;

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
        User mockUser = new User();
        mockUser.setPublicKey(publicKey);
        mockUser.setId("1");

        when(zkLoginVerifier.verifyProof(zkProof, publicKey)).thenReturn(true);
        when(userRepository.findByPublicKey(publicKey)).thenReturn(Optional.of(mockUser));

        try (var jwtUtilMock = mockStatic(JwtUtil.class)) {
            jwtUtilMock.when(() -> JwtUtil.generateAccessToken(mockUser.getId()))
                    .thenReturn("mocked_jwt_token");
            String jwt = authService.authenticateUser(zkLoginRequest);
            assertNotNull(jwt);
            assertEquals("mocked_jwt_token", jwt);
            verify(zkLoginVerifier).verifyProof(zkProof, publicKey);
            verify(userRepository).findByPublicKey(publicKey);
        }
    }

    @Test
    void testAuthenticateUser_InvalidProof() {
        when(zkLoginVerifier.verifyProof(zkProof, publicKey)).thenReturn(false);
        assertThrows(AuthenticationException.class, () -> authService.authenticateUser(zkLoginRequest));
        verify(zkLoginVerifier).verifyProof(zkProof, publicKey);
        verify(userRepository, never()).findByPublicKey(any());
    }

    @Test
    void testAuthenticateUser_UserNotFound() {
        when(zkLoginVerifier.verifyProof(eq(zkProof), eq(publicKey))).thenReturn(true);
        when(userRepository.findByPublicKey(eq(publicKey))).thenReturn(Optional.empty());
        AuthenticationException exception = assertThrows(AuthenticationException.class,
                () -> authService.authenticateUser(zkLoginRequest));
        assertEquals("User not found", exception.getMessage()); // Adjust message as per implementation
        verify(zkLoginVerifier).verifyProof(eq(zkProof), eq(publicKey));
        verify(userRepository).findByPublicKey(eq(publicKey));
    }

    @Test
    void testAuthenticateUser_NullRequest() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> authService.authenticateUser(null));
        assertEquals("Login request cannot be null", exception.getMessage()); // Adjust message as per implementation
        verifyNoInteractions(zkLoginVerifier, userRepository);
    }
}