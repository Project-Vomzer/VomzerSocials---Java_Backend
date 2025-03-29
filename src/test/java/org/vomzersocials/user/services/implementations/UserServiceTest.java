package org.vomzersocials.user.services.implementations;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.vomzersocials.user.data.models.User;
import org.vomzersocials.user.data.repositories.UserRepository;
import org.vomzersocials.user.dtos.requests.*;
import org.vomzersocials.user.dtos.responses.*;
import org.vomzersocials.user.data.models.Role;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Slf4j
public class UserServiceTest {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private UserRepository userRepository;

    private RegisterUserRequest registerUserRequest;
    private RegisterUserResponse registerUserResponse;
    private LoginRequest loginRequest;
    private LoginResponse loginResponse;
    private LogoutRequest logoutRequest;
    private LogoutUserResponse logoutUserResponse;
    private CreatePostRequest createPostRequest;
    private CreatePostResponse createPostResponse;
    private DeletePostRequest deletePostRequest;
    private DeletePostResponse deletePostResponse;

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

        createPostRequest = new CreatePostRequest();
        deletePostRequest = new DeletePostRequest();
    }

    @Test
    public void test_thatUserCanRegister_andCreatePost() {
        registerUserResponse = userService.registerNewUser(registerUserRequest);
        assertEquals("User registered successfully.", registerUserResponse.getMessage());

        loginResponse = userService.loginUser(loginRequest);
        assertEquals("Logged in successfully", loginResponse.getMessage());

        User user = new User();
        user.setUserName(loginResponse.getUserName());

        createPostRequest.setAuthor(user);
        createPostRequest.setTitle("Title");
        createPostRequest.setContent("Content");

        createPostResponse = userService.createPost(createPostRequest);
        assertNotNull(createPostResponse);
        assertEquals("Title", createPostResponse.getTitle());
        assertEquals("Content", createPostResponse.getContent());
        assertEquals("johni", createPostResponse.getAuthor().getUserName());
    }

    @Test
    public void test_thatUserCannotPostEmptyContent() {
        registerUserResponse = userService.registerNewUser(registerUserRequest);
        loginResponse = userService.loginUser(loginRequest);

        User testUser = userRepository.findUserByUserName(loginResponse.getUserName()).orElseThrow();

        CreatePostRequest postRequest = new CreatePostRequest();
        postRequest.setAuthor(testUser);
        postRequest.setTitle(" ");
        postRequest.setContent("");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                userService.createPost(postRequest));
        assertEquals("Author, title, and content are required", exception.getMessage());
    }

    @Test
    public void test_thatPostCreationFails_WhenUserNotFound() {
        CreatePostRequest postRequest = new CreatePostRequest();
        postRequest.setAuthor(new User()); // Creating a new user without saving it
        postRequest.setTitle("Test Title");
        postRequest.setContent("Test Content");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                userService.createPost(postRequest));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    public void test_thatUserNotLoggedIn_cannotCreateOrMakePost(){
        registerUserResponse = userService.registerNewUser(registerUserRequest);
        loginResponse = userService.loginUser(loginRequest);

        User testUser = userRepository.findUserByUserName(loginResponse.getUserName()).orElseThrow();
        createPostRequest.setAuthor(testUser);
        createPostRequest.setTitle("Sui");
        createPostRequest.setContent("A decentralised social media platform built on Java, React and Sui");
        createPostResponse = userService.createPost(createPostRequest);
        assertEquals("Sui", createPostResponse.getTitle());

        logoutUserResponse = userService.logoutUser(logoutRequest);
        assertEquals("Logged out successfully", logoutUserResponse.getMessage());

        createPostRequest.setAuthor(testUser);
        createPostRequest.setTitle("Sui-smart contract");
        createPostRequest.setContent("Cheapest gas fees and fast contract execution");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                userService.createPost(createPostRequest));
        assertEquals("User is not logged in", exception.getMessage());


    }
    @Test
    public void test_thatUserCanDeletePost(){
        registerUserResponse = userService.registerNewUser(registerUserRequest);
        loginResponse = userService.loginUser(loginRequest);
        User testUser = userRepository.findUserByUserName(loginResponse.getUserName()).orElseThrow();

        createPostRequest.setAuthor(testUser);
        createPostRequest.setTitle("Sui");
        createPostRequest.setContent("A decentralised social media platform built on Java, React and Sui");
        createPostResponse = userService.createPost(createPostRequest);
        assertEquals("Sui", createPostResponse.getTitle());

        log.info("Postid_test: {}", createPostResponse.getId());
        deletePostRequest.setPostId(createPostResponse.getId());
        deletePostRequest.setUserId(testUser.getId());

        deletePostResponse = userService.deletePost(deletePostRequest);
        assertEquals("Post deleted successfully", deletePostResponse.getMessage());
    }

    @Test
    public void test_thatUserCanEditPostCreated(){
        registerUserResponse = userService.registerNewUser(registerUserRequest);
        loginResponse = userService.loginUser(loginRequest);
        User testUser = userRepository.findUserByUserName(loginResponse.getUserName()).orElseThrow();

        createPostRequest.setAuthor(testUser);
        createPostRequest.setTitle("Sui");
        createPostRequest.setContent("A decentralised social media platform built on Java, React and Sui");
        createPostResponse = userService.createPost(createPostRequest);
        assertEquals("Sui", createPostResponse.getTitle());
    }





}
