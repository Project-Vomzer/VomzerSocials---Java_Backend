package org.vomzersocials.user.services.implementations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vomzersocials.user.data.repositories.UserRepository;
import org.vomzersocials.user.dtos.requests.LoginRequest;
import org.vomzersocials.user.dtos.requests.RegisterUserRequest;
import org.vomzersocials.user.dtos.responses.LoginResponse;
import org.vomzersocials.user.dtos.responses.RegisterUserResponse;
import org.vomzersocials.user.services.interfaces.AuthenticationService;
import org.vomzersocials.user.services.interfaces.UserService;


@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;
    private String foundUserUserName;
    @Autowired
    private AuthenticationService authenticationService;

    @Override
    public RegisterUserResponse registerNewUser(RegisterUserRequest registerUserRequest) {
        return authenticationService.registerNewUser(registerUserRequest);
    }

    @Override
    public LoginResponse loginUser(LoginRequest loginRequest) {
        return authenticationService.loginUser(loginRequest);
    }


}
