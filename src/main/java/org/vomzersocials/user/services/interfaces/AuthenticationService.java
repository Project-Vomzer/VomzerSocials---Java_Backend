package org.vomzersocials.user.services.interfaces;

import org.vomzersocials.user.dtos.requests.LoginRequest;
import org.vomzersocials.user.dtos.requests.LogoutRequest;
import org.vomzersocials.user.dtos.requests.RegisterUserRequest;
import org.vomzersocials.user.dtos.responses.LoginResponse;
import org.vomzersocials.user.dtos.responses.LogoutUserResponse;
import org.vomzersocials.user.dtos.responses.RegisterUserResponse;
import org.vomzersocials.user.dtos.responses.TokenPair;
import reactor.core.publisher.Mono;

public interface AuthenticationService {

    String generateAccessToken(String userId);
Mono<RegisterUserResponse> registerNewUser(RegisterUserRequest req);
    Mono<LoginResponse>      loginUser(LoginRequest req);
    Mono<LogoutUserResponse> logoutUser(LogoutRequest req);
    boolean validateAccessToken(String token);
    String generateRefreshToken(String userId);
    boolean validateRefreshToken(String token);
    Mono<TokenPair>          refreshTokens(String refreshToken);

}
