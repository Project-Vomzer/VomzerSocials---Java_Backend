package org.vomzersocials.user.services.interfaces;

import org.vomzersocials.user.dtos.requests.LoginRequest;
import org.vomzersocials.user.dtos.requests.LogoutRequest;
import org.vomzersocials.user.dtos.requests.RegisterUserRequest;
import org.vomzersocials.user.dtos.responses.LoginResponse;
import org.vomzersocials.user.dtos.responses.LogoutUserResponse;
import org.vomzersocials.user.dtos.responses.RegisterUserResponse;

public interface AuthenticationService {
    RegisterUserResponse registerNewUser(RegisterUserRequest registerUserRequest);
    LoginResponse loginUser(LoginRequest loginRequest);

    LogoutUserResponse logoutUser(LogoutRequest logoutUserRequest);
}
