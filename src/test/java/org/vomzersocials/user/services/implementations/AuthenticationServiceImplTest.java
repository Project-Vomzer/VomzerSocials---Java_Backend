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
import org.vomzersocials.user.dtos.responses.TokenPair;
import org.vomzersocials.user.enums.Role;
import org.vomzersocials.zkLogin.security.VerifiedAddressResult;
import org.vomzersocials.zkLogin.services.ZkLoginService;
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
        when(zkLoginService.loginViaZkProof(anyString(), anyString())).thenReturn(VerifiedAddressResult.success("mock-sui-address"));
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
    public void test_thatUserCanLoginWithStandardLoginDetails() {
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
    public void test_thatUserCanLoginWithZkLogin() {
        // Prepare register request
        registerUserRequest.setUserName("Johni1");
        registerUserRequest.setZkProof("mock-zk-proof");
        registerUserRequest.setPublicKey("mock-public-key");
        registerUserRequest.setPassword("Password@12");
        registerUserRequest.setRole(Role.ADMIN);

        // Mock zkLoginService for registration
        when(zkLoginService.registerViaZkProof(
                eq("mock-zk-proof"),
                eq("Johni1"),
                eq("mock-public-key"))
        ).thenReturn("mock-sui-address");

        // Mock zkLoginService for login (IMPORTANT: return String, not VerifiedAddressResult)
        when(zkLoginService.loginViaZkProof(
                eq("mock-zk-proof"),
                eq("mock-public-key"))
        ).thenReturn(VerifiedAddressResult.success("mock-sui-address"));

        // Mock save behavior
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Register the user
        RegisterUserResponse regResp = authenticationService.registerNewUser(registerUserRequest);
        assertEquals("User registered successfully.", regResp.getMessage());

        // Stub find by Sui address after registration
        User saved = new User();
        saved.setUserName("Johni1");
        saved.setSuiAddress("mock-sui-address");
        saved.setPassword("encodedPassword"); // password is needed if standard login was tested
        when(userRepository.findUserBySuiAddress("mock-sui-address"))
                .thenReturn(Optional.of(saved));

        // Now login
        LoginRequest zkLoginReq = new LoginRequest();
        zkLoginReq.setZkProof("mock-zk-proof");
        zkLoginReq.setPublicKey("mock-public-key");
        zkLoginReq.setLoginMethod("ZK_LOGIN");

        LoginResponse loginResp = authenticationService.loginUser(zkLoginReq);

        // Validate
        assertEquals("Logged in successfully", loginResp.getMessage());
    }




    @Test
    public void test_thatUserCanLogin_andLogout() {
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

    @Test
    public void test_thatWhenUserRegistersWithInvalidPasswordPattern_throwsException() {
        registerUserRequest.setPassword("weak");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                authenticationService.registerNewUser(registerUserRequest));
        assertEquals("Invalid username or password", exception.getMessage());
    }

    @Test
    public void test_thatUserCanLoginTheStandardWay_andUserIsNotFound_throwsException() {
        when(userRepository.findUserByUserName("Johni1")).thenReturn(Optional.empty());
        LoginRequest request = new LoginRequest();
        request.setLoginMethod("STANDARD_LOGIN");
        request.setUsername("Johni1");
        request.setPassword("Password@12");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                authenticationService.loginUser(request));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    public void test_refreshTokensGenerationIsSuccess() {
        when(jwtUtil.validateToken("good")).thenReturn(true);
        when(jwtUtil.extractUsername("good")).thenReturn("Johni1");
        when(jwtUtil.generateAccessToken("Johni1")).thenReturn("newA");
        when(jwtUtil.generateRefreshToken("Johni1")).thenReturn("newR");

        TokenPair pair = authenticationService.refreshTokens("good");
        assertEquals("newA", pair.getAccessToken());
        assertEquals("newR", pair.getRefreshToken());
    }

    @Test
    public void test_logoutUserNotFound_throwsException() {
        when(userRepository.findUserByUserName("Johni1")).thenReturn(Optional.empty());
        LogoutRequest request = new LogoutRequest();
        request.setUsername("Johni1");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                authenticationService.logoutUser(request));
        assertEquals("User not found", exception.getMessage());
    }

}
