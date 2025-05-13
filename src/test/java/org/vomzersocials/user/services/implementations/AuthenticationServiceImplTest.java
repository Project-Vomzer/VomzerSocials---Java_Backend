package org.vomzersocials.user.services.implementations;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.vomzersocials.user.data.models.User;
import org.vomzersocials.user.data.repositories.UserRepository;
import org.vomzersocials.user.dtos.requests.LoginRequest;
import org.vomzersocials.user.dtos.requests.LogoutRequest;
import org.vomzersocials.user.dtos.requests.RegisterUserRequest;
import org.vomzersocials.user.dtos.responses.LoginResponse;
import org.vomzersocials.user.dtos.responses.LogoutUserResponse;
import org.vomzersocials.user.dtos.responses.RegisterUserResponse;
import org.vomzersocials.user.dtos.responses.TokenPair;
import org.vomzersocials.user.enums.Role;
import org.vomzersocials.zkLogin.security.VerifiedAddressResult;
import org.vomzersocials.zkLogin.services.ZkLoginService;
import org.vomzersocials.user.springSecurity.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import reactor.test.StepVerifier;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Slf4j
public class AuthenticationServiceImplTest {

    @Mock private ZkLoginService zkLoginService;
    @Mock private JwtUtil jwtUtil;
    @Mock private UserRepository userRepository;
    @Mock private BCryptPasswordEncoder passwordEncoder;
    @InjectMocks private AuthenticationServiceImpl authenticationService;

    private RegisterUserRequest registerReq;
    private LoginRequest loginReq;
    private LogoutRequest logoutReq;

    @BeforeEach
    public void setUp() {
        registerReq = new RegisterUserRequest();
        registerReq.setUserName("Johni1");
        registerReq.setPassword("Password@12");
        registerReq.setZkProof("mock-zk-proof");
        registerReq.setPublicKey("mock-public-key");

        loginReq = new LoginRequest();
        loginReq.setUsername("Johni1");
        loginReq.setPassword("Password@12");
        loginReq.setLoginMethod("STANDARD_LOGIN");
        loginReq.setZkProof("mock-zk-proof");
        loginReq.setPublicKey("mock-public-key");

        logoutReq = new LogoutRequest();
        logoutReq.setUsername("Johni1");

        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        when(userRepository.findUserByUserName("Johni1")).thenReturn(Optional.empty());
        when(zkLoginService.registerViaZkProof(eq("mock-zk-proof"), eq("Johni1"), eq("mock-public-key")))
                .thenReturn("mock-sui-address");
        when(zkLoginService.loginViaZkProof(anyString(), anyString()))
                .thenReturn(VerifiedAddressResult.success("mock-sui-address"));
        when(jwtUtil.generateAccessToken(anyString(), anyList()))
                .thenReturn("mock-access-token");
        when(jwtUtil.generateRefreshToken(anyString()))
                .thenReturn("mock-refresh-token");
    }

    @Test
    public void test_thatUserCanRegister() {
        RegisterUserResponse resp = authenticationService.registerNewUser(registerReq).block();
        assertNotNull(resp);
        assertEquals("User registered successfully.", resp.getMessage());
    }

    @Test
    void test_thatUserCanRegister2() {
        StepVerifier.create(authenticationService.registerNewUser(registerReq))
                .assertNext(resp -> {
                    assertEquals("User registered successfully.", resp.getMessage());
                })
                .verifyComplete();
    }


    @Test
    public void test_thatUserCannotRegisterANullValue() {
        registerReq.setUserName(" ");
        registerReq.setPassword(" ");
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authenticationService.registerNewUser(registerReq).block()
        );
        assertEquals("Invalid zk-proof or proof verification failed", ex.getMessage());
    }

    @Test
    public void test_thatUserCanLoginWithStandardLoginDetails() {
        authenticationService.registerNewUser(registerReq).block();
        User dummy = new User();
        dummy.setUserName("Johni1");
        dummy.setPassword("encoded-password");
        when(userRepository.findUserByUserName("Johni1"))
                .thenReturn(Optional.of(dummy));
        when(passwordEncoder.matches("Password@12", "encoded-password"))
                .thenReturn(true);
        when(jwtUtil.generateAccessToken("Johni1")).thenReturn("mock-access-token");
        when(jwtUtil.generateRefreshToken("Johni1")).thenReturn("mock-refresh-token");

        LoginResponse loginResp = authenticationService.loginUser(loginReq).block();
        assertNotNull(loginResp);
        assertEquals("Logged in successfully", loginResp.getMessage());
        when(jwtUtil.generateAccessToken("Johni1")).thenReturn("mock-access-token");
        when(jwtUtil.generateRefreshToken("Johni1")).thenReturn("mock-refresh-token");
        verify(userRepository).save(argThat(u -> "Johni1".equals(u.getUserName()) && Boolean.TRUE.equals(u.getIsLoggedIn())));
    }

    @Test
    public void test_thatUserCanLoginWithZkLogin() {
        authenticationService.registerNewUser(registerReq).block();

        when(userRepository.findUserBySuiAddress("mock-sui-address"))
                .thenReturn(Optional.of(new User(){{
                    setUserName("Johni1");
                    setSuiAddress("mock-sui-address");
                }}));
        LoginRequest zkReq = new LoginRequest();
        zkReq.setLoginMethod("ZK_LOGIN");
        zkReq.setZkProof("mock-zk-proof");
        zkReq.setPublicKey("mock-public-key");

        LoginResponse resp = authenticationService.loginUser(zkReq).block();
        assertNotNull(resp);
        assertEquals("Logged in successfully", resp.getMessage());
    }

    @Test
    public void test_thatUserCanLogin_andLogout() {
        User u = new User();
        u.setUserName("Johni1");
        u.setIsLoggedIn(true);
        when(userRepository.findUserByUserName("Johni1"))
                .thenReturn(Optional.of(u));

        LogoutUserResponse out = authenticationService.logoutUser(logoutReq).block();
        assertEquals("Logged out successfully", out.getMessage());

        verify(userRepository).save(argThat(saved ->
                "Johni1".equals(saved.getUserName()) && Boolean.FALSE.equals(saved.getIsLoggedIn())
        ));
    }

    @Test
    public void test_thatStandardLoginUserNotFound_throwsException() {
        when(userRepository.findUserByUserName("Johni1")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authenticationService.loginUser(loginReq).block()
        );
        assertEquals("Invalid username or password", ex.getMessage());
    }

    @Test
    public void test_refreshTokensGenerationIsSuccess() {
        User dummy = new User();
        dummy.setUserName("Johni1");
        when(userRepository.findUserByUserName("Johni1")).thenReturn(Optional.of(dummy));
        when(jwtUtil.validateToken("good")).thenReturn(true);
        when(jwtUtil.extractUsername("good")).thenReturn("Johni1");
        when(jwtUtil.generateAccessToken("Johni1")).thenReturn("newA");
        when(jwtUtil.generateRefreshToken("Johni1")).thenReturn("newR");

        TokenPair pair = authenticationService.refreshTokens("good").block();
        assertNotNull(pair);
        assertEquals("newA", pair.getAccessToken());
        assertEquals("newR", pair.getRefreshToken());
    }

    @Test
    public void test_logoutUserNotFound_throwsException() {
        when(userRepository.findUserByUserName("Johni1")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authenticationService.logoutUser(logoutReq).block()
        );
        assertEquals("User not found", ex.getMessage());
    }

    @Test
    public void test_thatInvalidZkProofLogin_throwsException() {
        when(zkLoginService.loginViaZkProof("bad-zk-proof", "mock-public-key"))
                .thenReturn(null);

        LoginRequest bad = new LoginRequest();
        bad.setLoginMethod("ZK_LOGIN");
        bad.setZkProof("bad-zk-proof");
        bad.setPublicKey("mock-public-key");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authenticationService.loginUser(bad).block()
        );
        assertEquals("Invalid zk-proof or proof verification failed", ex.getMessage());
    }
}
