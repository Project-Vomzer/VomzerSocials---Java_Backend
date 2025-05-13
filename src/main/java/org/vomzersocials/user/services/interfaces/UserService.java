package org.vomzersocials.user.services.interfaces;

import org.vomzersocials.user.data.models.User;
import org.vomzersocials.user.data.repositories.FollowRepository;
import org.vomzersocials.user.dtos.requests.*;
import org.vomzersocials.user.dtos.responses.*;
import org.vomzersocials.user.enums.FollowType;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

public interface UserService {
//    RegisterUserResponse registerNewUser(RegisterUserRequest registerUserRequest);
//    LoginResponse loginUser(LoginRequest loginRequest);
//    LogoutUserResponse logoutUser(LogoutRequest logoutUserRequest);
//    CreatePostResponse createPost(CreatePostRequest createPostRequest);
//    DeletePostResponse deletePost(DeletePostRequest deletePostRequest);
//    EditPostResponse editPost(EditPostRequest editPostRequest);
//
//    CreatePostResponse createPost(CreatePostRequest createPostRequest, String userId);
//
//    DeletePostResponse deletePost(DeletePostRequest deletePostRequest, String userId);
//
//    EditPostResponse editPost(EditPostRequest editPostRequest, String userId);

    int updateUserFollowCount(FollowUserRequest request, FollowType followType, boolean isIncrement);
    Optional<User> findById(String followingId);
    void saveAll(List<User> users);
    List<User> findAll();

        Mono<RegisterUserResponse> registerNewUser(RegisterUserRequest request);
        Mono<LoginResponse> loginUser(LoginRequest request);
        Mono<LogoutUserResponse> logoutUser(LogoutRequest request);
        Mono<CreatePostResponse> createPost(CreatePostRequest request, String userId);
        Mono<DeletePostResponse> deletePost(DeletePostRequest request, String userId);
        Mono<EditPostResponse> editPost(EditPostRequest request, String userId);
        Mono<RepostResponse> repost(RepostRequest request, String userId);
}
