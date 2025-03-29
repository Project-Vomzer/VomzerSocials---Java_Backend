package org.vomzersocials.user.services.implementations;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.vomzersocials.user.data.repositories.UserRepository;
import org.vomzersocials.user.dtos.requests.LoginRequest;
import org.vomzersocials.user.dtos.requests.LogoutRequest;
import org.vomzersocials.user.dtos.responses.LogoutUserResponse;
import org.vomzersocials.user.dtos.requests.RegisterUserRequest;
import org.vomzersocials.user.dtos.responses.LoginResponse;
import org.vomzersocials.user.dtos.responses.RegisterUserResponse;
import org.vomzersocials.user.utils.Role;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AuthenticationServiceImplTest {
    @Autowired
    private UserServiceImpl userService;

    private RegisterUserRequest registerUserRequest;
    private RegisterUserResponse registerUserResponse;
    private LoginRequest loginRequest;
    private LoginResponse loginResponse;
    private LogoutRequest logoutRequest;
    private LogoutUserResponse logoutUserResponse;
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();
        registerUserRequest = new RegisterUserRequest();
        registerUserRequest.setUserName("johni");
        registerUserRequest.setPassword("password");
        registerUserRequest.setRole(Role.ADMIN);

        loginRequest = new LoginRequest();
        loginRequest.setUsername("johni");
        loginRequest.setPassword("password");

        logoutRequest = new LogoutRequest();
        logoutRequest.setUserName("johni");

    }

    @Test
    public void test_thatUserCanRegister() {
        registerUserResponse = userService.registerNewUser(registerUserRequest);
        assertEquals("User registered successfully.", registerUserResponse.getMessage());
    }

    @Test
    public void test_thatUserCannotRegisterANullValue() {
        registerUserRequest.setUserName(" ");
        registerUserRequest.setPassword(" ");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                userService.registerNewUser(registerUserRequest));
        assertEquals("Username and password are required", exception.getMessage());
    }

    @Test
    public void test_thatUserCannotRegisterNullValue(){
        registerUserRequest.setUserName("");
        registerUserRequest.setPassword("");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                userService.registerNewUser(registerUserRequest));
        assertEquals("Username and password are required", exception.getMessage());
    }

    @Test
    public void test_thatUserCanLogin(){
        registerUserResponse = userService.registerNewUser(registerUserRequest);
        assertEquals("User registered successfully.", registerUserResponse.getMessage());

        loginResponse = userService.loginUser(loginRequest);
        assertEquals("Logged in successfully", loginResponse.getMessage());
    }

    @Test
    public void staffWhenLoggedInCanLogout_test(){
        userService.registerNewUser(registerUserRequest);
        userService.loginUser(loginRequest);

        logoutUserResponse = userService.logoutUser(logoutRequest);
        assertEquals("Logged out successfully", logoutUserResponse.getMessage());
    }



}