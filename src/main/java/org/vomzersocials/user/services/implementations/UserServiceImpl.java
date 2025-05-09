package org.vomzersocials.user.services.implementations;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vomzersocials.user.data.models.User;
import org.vomzersocials.user.data.repositories.UserRepository;
import org.vomzersocials.user.dtos.requests.*;
import org.vomzersocials.user.dtos.responses.*;
import org.vomzersocials.user.enums.FollowType;
import org.vomzersocials.user.services.interfaces.AuthenticationService;
import org.vomzersocials.user.services.interfaces.PostService;
import org.vomzersocials.user.services.interfaces.UserService;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private final AuthenticationService authenticationService;
    private final PostService postService;
    @Autowired
    private UserRepository userRepository;

    public UserServiceImpl(AuthenticationService authenticationService, PostService postService) {
        this.authenticationService = authenticationService;
        this.postService = postService;
    }

    @Override
    public RegisterUserResponse registerNewUser(RegisterUserRequest registerUserRequest) {
        return authenticationService.registerNewUser(registerUserRequest).block();
    }

    @Override
    public LoginResponse loginUser(LoginRequest loginRequest) {
        return authenticationService.loginUser(loginRequest).block();
    }

    @Override
    public LogoutUserResponse logoutUser(LogoutRequest logoutUserRequest) {
        return authenticationService.logoutUser(logoutUserRequest).block();
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

    @Override
    public int updateUserFollowCount(FollowUserRequest request, FollowType followType, boolean isIncrement) {
        User user;

        if (followType == FollowType.FOLLOWER) {
            user = userRepository.findUserById(request.getFollowingId());
            int newCount = user.getFollowerCount() + (isIncrement ? 1 : -1);
            user.setFollowerCount(Math.max(newCount, 0));
            return userRepository.save(user).getFollowerCount();
        } else if (followType == FollowType.FOLLOWING) {
            user = userRepository.findUserById(request.getFollowerId());
            int newCount = user.getFollowingCount() + (isIncrement ? 1 : -1);
            user.setFollowingCount(Math.max(newCount, 0));
            return userRepository.save(user).getFollowingCount();
        } else {
            throw new IllegalArgumentException("Unknown follow type");
        }
    }

    @Override
    public Optional<User> findById(String followingId) {
        return userRepository.findById(followingId);
    }

    @Override
    public void saveAll(List<User> users) {
        if (users != null && !users.isEmpty()) {
            userRepository.saveAll(users);
        }
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }


}
