package org.vomzersocials.user.services.implementations;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.vomzersocials.user.dtos.requests.*;
import org.vomzersocials.user.dtos.responses.*;
import org.vomzersocials.user.services.interfaces.AuthenticationService;
import org.vomzersocials.user.services.interfaces.PostService;
import org.vomzersocials.user.services.interfaces.UserService;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceImplTest {

    private AuthenticationService authenticationService;
    private PostService postService;
    private UserService userService;

    @BeforeEach
    void setUp() {
        authenticationService = mock(AuthenticationService.class);
        postService = mock(PostService.class);
        userService = new UserServiceImpl(authenticationService, postService);
    }

    @Test
    void testRegisterNewUser() {
        RegisterUserRequest request = new RegisterUserRequest();
        RegisterUserResponse expectedResponse = new RegisterUserResponse();
        expectedResponse.setMessage("User registered successfully");

        when(authenticationService.registerNewUser(request)).thenReturn(Mono.just(expectedResponse));

        RegisterUserResponse actualResponse = userService.registerNewUser(request);

        assertNotNull(actualResponse);
        assertEquals("User registered successfully", actualResponse.getMessage());
        verify(authenticationService, times(1)).registerNewUser(request);
    }

    @Test
    void testLoginUser() {
        LoginRequest request = new LoginRequest();
        LoginResponse expectedResponse = new LoginResponse();
        expectedResponse.setToken("jwt-token");

        when(authenticationService.loginUser(request)).thenReturn(Mono.just(expectedResponse));

        LoginResponse actualResponse = userService.loginUser(request);

        assertNotNull(actualResponse);
        assertEquals("jwt-token", actualResponse.getToken());
        verify(authenticationService, times(1)).loginUser(request);
    }

    @Test
    void testLogoutUser() {
        LogoutRequest request = new LogoutRequest();
        LogoutUserResponse expectedResponse = new LogoutUserResponse();
        expectedResponse.setMessage("Logout successful");

        when(authenticationService.logoutUser(request)).thenReturn(Mono.just(expectedResponse));

        LogoutUserResponse actualResponse = userService.logoutUser(request);

        assertNotNull(actualResponse);
        assertEquals("Logout successful", actualResponse.getMessage());
        verify(authenticationService, times(1)).logoutUser(request);
    }

    @Test
    void testCreatePost() {
        CreatePostRequest request = new CreatePostRequest();
        CreatePostResponse expectedResponse = new CreatePostResponse();
        expectedResponse.setMessage("Post created");

        when(postService.createPost(request)).thenReturn(expectedResponse);

        CreatePostResponse actualResponse = userService.createPost(request);

        assertNotNull(actualResponse);
        assertEquals("Post created", actualResponse.getMessage());
        verify(postService, times(1)).createPost(request);
    }

    @Test
    void testDeletePost() {
        DeletePostRequest request = new DeletePostRequest();
        DeletePostResponse expectedResponse = new DeletePostResponse();
        expectedResponse.setMessage("Post deleted");

        when(postService.deletePost(request)).thenReturn(expectedResponse);

        DeletePostResponse actualResponse = userService.deletePost(request);

        assertNotNull(actualResponse);
        assertEquals("Post deleted", actualResponse.getMessage());
        verify(postService, times(1)).deletePost(request);
    }

    @Test
    void testEditPost() {
        EditPostRequest request = new EditPostRequest();
        EditPostResponse expectedResponse = new EditPostResponse();
        expectedResponse.setMessage("Post edited");

        when(postService.editPost(request)).thenReturn(expectedResponse);

        EditPostResponse actualResponse = userService.editPost(request);

        assertNotNull(actualResponse);
        assertEquals("Post edited", actualResponse.getMessage());
        verify(postService, times(1)).editPost(request);
    }
}
