package org.vomzersocials.services.implementations;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.vomzersocials.authentication.dtos.requests.LoginRequest;
import org.vomzersocials.authentication.dtos.responses.LoginResponse;
import org.vomzersocials.user.data.repositories.UserRepository;
import org.vomzersocials.user.dtos.requests.RegisterUserRequest;
import org.vomzersocials.user.dtos.responses.RegisterUserResponse;
import org.vomzersocials.user.services.implementations.UserService;
import org.vomzersocials.utils.Role;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UserServiceTest {
    @Autowired
    private UserService userService;

    private RegisterUserRequest registerUserRequest;
    private RegisterUserResponse registerUserResponse;
    private LoginRequest loginRequest;
    private LoginResponse loginResponse;
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();
        registerUserRequest = new RegisterUserRequest();
        registerUserRequest.setUserName("johni");
        registerUserRequest.setPassword("password");
        registerUserRequest.setRole(Role.ADMIN);
        registerUserRequest.setIsLoggedIn(false);

//        loginRequest.setUsername("johni");
//        loginRequest.setPassword("password");
//

    }

    @Test
    public void test_thatUserCanRegister() {
        registerUserResponse = userService.registerNewUser(registerUserRequest);
        assertEquals("User is registered in successfully.", registerUserResponse.getMessage());
    }

    @Test
    public void test_thatUserCannotRegisterANullValue() {
        registerUserRequest.setUserName(" ");
        registerUserRequest.setPassword(" ");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                userService.registerNewUser(registerUserRequest));
        assertEquals("Username and password are required!", exception.getMessage());
    }

    @Test
    public void test_thatUserCannotRegisterNullValue(){
        registerUserRequest.setUserName("");
        registerUserRequest.setPassword("");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                userService.registerNewUser(registerUserRequest));
        assertEquals("Username or password cannot be empty", exception.getMessage());
    }

//    @Test
//    public void test_thatUserCanLogin(){
//        registerUserResponse = userService.registerNewUser(registerUserRequest);
//        assertEquals("User is registered in successfully.", registerUserResponse.getMessage());
//
//    }


}