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
import org.vomzersocials.user.enums.Role;
import org.vomzersocials.zkLogin.dtos.ZkLoginRequest;
import org.vomzersocials.user.springSecurity.JwtUtil;
import org.vomzersocials.zkLogin.services.ZkLoginAuthService;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ZkLoginVerifier zkLoginVerifier;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private ZkLoginAuthService authService;

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
    public void testAuthenticateUser_Success() {
        User mockUser = new User();
        mockUser.setPublicKey(publicKey);
        mockUser.setUserName("john");

        when(zkLoginVerifier.verifyProof(zkProof, publicKey)).thenReturn(Mono.just(true));
        when(userRepository.findByPublicKey(publicKey)).thenReturn(Optional.of(mockUser));
        when(jwtUtil.generateAccessToken("john", List.of("SUBSCRIBER")))
                .thenReturn("mocked_jwt_token");

        StepVerifier.create(authService.authenticateUser(zkLoginRequest))
                .expectNext("mocked_jwt_token")
                .verifyComplete();

        verify(zkLoginVerifier).verifyProof(zkProof, publicKey);
        verify(userRepository).findByPublicKey(publicKey);
        verify(jwtUtil).generateAccessToken("john", List.of("SUBSCRIBER"));
    }

    @Test
    public void testAuthenticateUser_InvalidProof() {
        when(zkLoginVerifier.verifyProof(zkProof, publicKey)).thenReturn(Mono.just(false));

        StepVerifier.create(authService.authenticateUser(zkLoginRequest))
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().equals("Invalid zkLogin proof"))
                .verify();

        verify(zkLoginVerifier).verifyProof(zkProof, publicKey);
        verify(userRepository, never()).findByPublicKey(any());
    }

    @Test
    public void testAuthenticateUser_UserNotFound() {
        when(zkLoginVerifier.verifyProof(zkProof, publicKey)).thenReturn(Mono.just(true));
        when(userRepository.findByPublicKey(publicKey)).thenReturn(Optional.empty());

        StepVerifier.create(authService.authenticateUser(zkLoginRequest))
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().equals("User not found"))
                .verify();

        verify(zkLoginVerifier).verifyProof(zkProof, publicKey);
        verify(userRepository).findByPublicKey(publicKey);
    }

    @Test
    public void testAuthenticateUser_NullRequest() {
        StepVerifier.create(authService.authenticateUser(null))
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().equals("Login request cannot be null"))
                .verify();

        verifyNoInteractions(zkLoginVerifier, userRepository);
    }
}
