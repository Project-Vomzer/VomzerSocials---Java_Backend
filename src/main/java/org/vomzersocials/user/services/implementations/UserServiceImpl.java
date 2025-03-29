package org.vomzersocials.user.services.implementations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vomzersocials.user.data.models.User;
import org.vomzersocials.user.data.repositories.PostRepository;
import org.vomzersocials.user.data.repositories.UserRepository;
import org.vomzersocials.user.dtos.requests.CreatePostRequest;
import org.vomzersocials.user.dtos.requests.LoginRequest;
import org.vomzersocials.user.dtos.requests.LogoutRequest;
import org.vomzersocials.user.dtos.requests.RegisterUserRequest;
import org.vomzersocials.user.dtos.responses.CreatePostResponse;
import org.vomzersocials.user.dtos.responses.LoginResponse;
import org.vomzersocials.user.dtos.responses.LogoutUserResponse;
import org.vomzersocials.user.dtos.responses.RegisterUserResponse;
import org.vomzersocials.user.services.interfaces.AuthenticationService;
import org.vomzersocials.user.services.interfaces.UserService;
import org.vomzersocials.user.utils.Post;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private PostRepository postRepository;

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
        if (createPostRequest.getAuthor() == null ||
                createPostRequest.getTitle() == null || createPostRequest.getTitle().trim().isEmpty() ||
                createPostRequest.getContent() == null || createPostRequest.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Author, title, and content are required");
        }

        User user = userRepository.findUserByUserName(createPostRequest.getAuthor().getUserName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!user.getIsLoggedIn()) throw new IllegalArgumentException("User is not logged in");

        Post post = new Post();
        post.setAuthor(user);
        post.setTitle(createPostRequest.getTitle());
        post.setContent(createPostRequest.getContent());
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());

        Post savedPost = postRepository.save(post);

        CreatePostResponse createPostResponse = new CreatePostResponse();
        createPostResponse.setAuthor(savedPost.getAuthor());
        createPostResponse.setTimestamp(savedPost.getCreatedAt());
        createPostResponse.setTitle(savedPost.getTitle());
        createPostResponse.setContent(savedPost.getContent());
        return createPostResponse;
    }



}
