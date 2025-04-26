package org.vomzersocials.user.services.implementations;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.vomzersocials.user.data.models.User;
import org.vomzersocials.user.data.repositories.UserRepository;
import org.vomzersocials.user.dtos.requests.LoginRequest;
import org.vomzersocials.user.dtos.requests.LogoutRequest;
import org.vomzersocials.user.dtos.requests.RegisterUserRequest;
import org.vomzersocials.user.dtos.responses.LoginResponse;
import org.vomzersocials.user.dtos.responses.LogoutUserResponse;
import org.vomzersocials.user.dtos.responses.RegisterUserResponse;
import org.vomzersocials.user.enums.Role;
import org.vomzersocials.zkLogin.services.ZkLoginService;
import org.vomzersocials.zkLogin.security.SuiZkLoginClient;
import org.vomzersocials.user.springSecurity.JwtUtil;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
public class AuthenticationServiceImplTest {

    @Mock
    private ZkLoginService zkLoginService;

    @Mock
    private SuiZkLoginClient suiZkLoginClient;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private RegisterUserRequest registerUserRequest;
    private RegisterUserResponse registerUserResponse;
    private LoginRequest loginRequest;
    private LoginResponse loginResponse;
    private LogoutUserResponse logoutUserResponse;

    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();
        registerUserRequest = new RegisterUserRequest();
        registerUserRequest.setUserName("Johni1");
        registerUserRequest.setPassword("Password@12");
        registerUserRequest.setRole(Role.ADMIN);
        registerUserRequest.setZkProof("mock-zk-proof");
        registerUserRequest.setPublicKey("mock-public-key");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("Johni1");
        loginRequest.setPassword("Password@12");
        loginRequest.setZkProof("mock-zk-proof");
        loginRequest.setPublicKey("mock-public-key");

        LogoutRequest logoutRequest = new LogoutRequest();
        logoutRequest.setUsername("Johni1");

        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        when(userRepository.findUserByUserName("Johni1")).thenReturn(Optional.empty());
        when(zkLoginService.registerViaZkProof(
                eq("mock-zk-proof"),
                eq("Johni1"),
                eq("mock-public-key"))
        ).thenReturn("mock-sui-address");
        when(zkLoginService.loginViaZkProof(anyString(), anyString())).thenReturn("mock-sui-address");
        when(jwtUtil.generateAccessToken(anyString())).thenReturn("mock-access-token");
        when(jwtUtil.generateRefreshToken(anyString())).thenReturn("mock-refresh-token");
    }



    @Test
    public void test_thatUserCanRegister() {
        RegisterUserResponse response = authenticationService.registerNewUser(registerUserRequest);
        assertEquals("User registered successfully.", response.getMessage());
    }

    @Test
    public void test_thatUserCannotRegisterANullValue() {
        registerUserRequest.setUserName(" ");
        registerUserRequest.setPassword(" ");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                authenticationService.registerNewUser(registerUserRequest));
        assertEquals("Invalid username or password", exception.getMessage());
    }

    @Test
    public void test_userCanLoginWithStandardLogin() {
        RegisterUserResponse reg = authenticationService.registerNewUser(registerUserRequest);
        assertEquals("User registered successfully.", reg.getMessage());

        User dummyUser = new User();
        dummyUser.setUserName("Johni1");
        dummyUser.setPassword("encoded-password");

        when(userRepository.findUserByUserName("Johni1"))
                .thenReturn(Optional.of(dummyUser));
        when(passwordEncoder.matches("Password@12", "encoded-password"))
                .thenReturn(true);
        LoginRequest standardLogin = new LoginRequest();
        standardLogin.setUsername("Johni1");
        standardLogin.setPassword("Password@12");
        standardLogin.setLoginMethod("STANDARD_LOGIN");

        LoginResponse response = authenticationService.loginUser(standardLogin);

        assertEquals("Logged in successfully", response.getMessage());
        assertEquals("mock-access-token", response.getAccessToken());
        verify(userRepository).save(argThat(user -> user.getUserName().equals("Johni1") && user.getIsLoggedIn()));
    }

    @Test
    public void test_userCanLoginWithZkLogin() {
        RegisterUserResponse registerUserResponse = authenticationService.registerNewUser(registerUserRequest);
        assertEquals("User registered successfully.", registerUserResponse.getMessage());

        User dummyUser = new User();
        dummyUser.setUserName(registerUserRequest.getUserName());
        dummyUser.setSuiAddress("mock-sui-address");
        dummyUser.setPassword("encoded-password"); // or whatever you like
        when(userRepository.findUserBySuiAddress("mock-sui-address"))
                .thenReturn(Optional.of(dummyUser));
        LoginRequest zkLoginRequest = new LoginRequest();
        zkLoginRequest.setZkProof("mock-zk-proof");
        zkLoginRequest.setPublicKey("mock-public-key");
        zkLoginRequest.setLoginMethod("ZK_LOGIN");

        loginResponse = authenticationService.loginUser(zkLoginRequest);
        assertEquals("Logged in successfully", loginResponse.getMessage());
    }

    @Test
    public void test_userCanLogout() {
        User dummyUser = new User();
        dummyUser.setUserName("Johni1");
        dummyUser.setIsLoggedIn(true);
        when(userRepository.findUserByUserName("Johni1"))
                .thenReturn(Optional.of(dummyUser));
        LogoutRequest logoutRequest = new LogoutRequest();
        logoutRequest.setUsername("Johni1");
        LogoutUserResponse resp = authenticationService.logoutUser(logoutRequest);

        assertEquals("Logged out successfully", resp.getMessage());
        verify(userRepository).save(argThat(user ->
                user.getUserName().equals("Johni1") && !user.getIsLoggedIn()
        ));
    }

}
