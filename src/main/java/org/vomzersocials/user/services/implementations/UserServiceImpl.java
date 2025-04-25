package org.vomzersocials.user.services.implementations;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.vomzersocials.user.dtos.requests.*;
import org.vomzersocials.user.dtos.responses.*;
import org.vomzersocials.user.services.interfaces.AuthenticationService;
import org.vomzersocials.user.services.interfaces.PostService;
import org.vomzersocials.user.services.interfaces.UserService;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private final AuthenticationService authenticationService;
    private final PostService postService;

    public UserServiceImpl( AuthenticationService authenticationService, PostService postService) {
        this.authenticationService = authenticationService;
        this.postService = postService;
    }



    @Override
    public RegisterUserResponse registerNewUser(RegisterUserRequest registerUserRequest) {
        return authenticationService.registerNewUser(registerUserRequest);
    }

    @Override
    public LoginResponse loginUser(LoginRequest loginRequest) {
        return authenticationService.loginUser(loginRequest);
    }

    @Override
    public LogoutUserResponse logoutUser(LogoutRequest logoutUserRequest) {
        return authenticationService.logoutUser(logoutUserRequest);
    }

    @Override
    public CreatePostResponse createPost(CreatePostRequest createPostRequest) {
        return postService.createPost(createPostRequest);
    }

    @Override
    public DeletePostResponse deletePost(DeletePostRequest deletePostRequest) {
        return postService.deletePost(deletePostRequest);
    }

    @Override
    public EditPostResponse editPost(EditPostRequest editPostRequest) {
        return postService.editPost(editPostRequest);
    }



}
