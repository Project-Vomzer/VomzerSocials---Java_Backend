package org.vomzersocials.user.services.implementations;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.vomzersocials.user.dtos.requests.LoginRequest;
import org.vomzersocials.user.dtos.responses.LoginResponse;
import org.vomzersocials.user.data.repositories.UserRepository;
import org.vomzersocials.user.dtos.requests.RegisterUserRequest;
import org.vomzersocials.user.dtos.responses.RegisterUserResponse;
import org.vomzersocials.user.services.interfaces.UserService;
import org.vomzersocials.user.utils.Role;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UserServiceTest {
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    private RegisterUserRequest registerUserRequest;
    private RegisterUserResponse registerUserResponse;
    private LoginRequest loginRequest;
    private LoginResponse loginResponse;


    @BeforeEach
    public void setUp() {
        registerUserRequest = new RegisterUserRequest();
        registerUserRequest.setUserName("Abi");
        registerUserRequest.setPassword("pass");
        registerUserRequest.setRole(Role.ADMIN);

        loginRequest = new LoginRequest();
        loginRequest.setUsername("Abi");
        loginRequest.setPassword("pass");
        loginRequest.setRole(Role.ADMIN);
    }

    @Test
    public void testRegisteredUserCanCreatePost() {
        RegisterUserResponse response = userService.registerNewUser(registerUserRequest);
        assertEquals("User is registered in successfully.", response.getMessage());
        LoginResponse userLogin = userService.loginUser(loginRequest);
        assertEquals("User is logged in successfully.", userLogin.getMessage());


    }
}