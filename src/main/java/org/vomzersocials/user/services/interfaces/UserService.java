package org.vomzersocials.user.services.interfaces;

import org.vomzersocials.user.dtos.requests.*;
import org.vomzersocials.user.dtos.responses.*;

public interface UserService {
    RegisterUserResponse registerNewUser(RegisterUserRequest registerUserRequest);
    LoginResponse loginUser(LoginRequest loginRequest);
    LogoutUserResponse logoutUser(LogoutRequest logoutUserRequest);
    CreatePostResponse createPost(CreatePostRequest createPostRequest);
    DeletePostResponse deletePost(DeletePostRequest deletePostRequest);
}
