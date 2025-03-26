package org.vomzersocials.user.services.interfaces;

import org.vomzersocials.user.dtos.requests.RegisterUserRequest;
import org.vomzersocials.user.dtos.responses.RegisterUserResponse;

public interface UserService {
    RegisterUserResponse registerNewUser(RegisterUserRequest registerUserRequest);
}
