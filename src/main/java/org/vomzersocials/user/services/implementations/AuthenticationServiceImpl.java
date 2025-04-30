package org.vomzersocials.user.services.implementations;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.vomzersocials.user.services.interfaces.AuthenticationService;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;
import org.vomzersocials.user.data.models.User;
import org.vomzersocials.user.data.repositories.UserRepository;
import org.vomzersocials.user.dtos.requests.LoginRequest;
import org.vomzersocials.user.dtos.requests.LogoutRequest;
import org.vomzersocials.user.dtos.requests.RegisterUserRequest;
import org.vomzersocials.user.dtos.responses.LoginResponse;
import org.vomzersocials.user.dtos.responses.LogoutUserResponse;
import org.vomzersocials.user.dtos.responses.RegisterUserResponse;
import org.vomzersocials.user.dtos.responses.TokenPair;
import org.vomzersocials.user.enums.LoginMethod;
import org.vomzersocials.zkLogin.security.VerifiedAddressResult;
import org.vomzersocials.zkLogin.services.ZkLoginService;
import org.vomzersocials.user.springSecurity.JwtUtil;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.vomzersocials.user.utils.ValidationUtils.isValidPassword;
import static org.vomzersocials.user.utils.ValidationUtils.isValidUsername;

@Service
@Transactional
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ZkLoginService zkLoginService;
    private final WalletApiClient walletApiClient;
    private final JwtUtil jwtUtil;

    public AuthenticationServiceImpl(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, ZkLoginService zkLoginService,
                                     WalletApiClient walletApiClient, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.zkLoginService = zkLoginService;
        this.walletApiClient = walletApiClient;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<RegisterUserResponse> registerNewUser(RegisterUserRequest request) {
        try {
//            validateUserInput(request.getUserName(), request.getPassword());
            return findExistingUser(request.getUserName())
                    .flatMap(existingUser -> {
                        if (existingUser.isPresent()) {
                            return Mono.error(new IllegalArgumentException("Username already exists"));
                        }
                        return getGeneratedSuiAddress(request)
                                .flatMap(address -> Mono.fromCallable(() -> {
                                    User user = createUser(request, address);
                                    return registerNewUserResponse(request, user);
                                }).subscribeOn(Schedulers.boundedElastic()));
                    });
        } catch (Exception ex) {
            return Mono.error(ex);
        }
    }

    private void validateUserInput(String username, String password) {
        boolean isZkLogin = password == null || password.isEmpty();
        if (!isValidUsername(username) || (!isZkLogin && !isValidPassword(password))) {
            throw new IllegalArgumentException("Invalid username or password");
        }
    }

    private Mono<Optional<User>> findExistingUser(String userName) {
        return Mono.fromCallable(() -> userRepository.findUserByUserName(userName))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private Mono<String> getGeneratedSuiAddress(RegisterUserRequest request) {
        if (request.getZkProof() != null && !request.getZkProof().isEmpty()) {
            String suiAddress = verifyZkProofAndRegisterOrThrow(
                    request.getZkProof(), request.getUserName(), request.getPublicKey());
            return Mono.just(suiAddress);
        } else {
            return walletApiClient.generateSuiAddress();
        }
    }

    @Override
    public Mono<LoginResponse> loginUser(LoginRequest request) {
        return Mono.fromCallable(() -> {
                    LoginMethod loginMethod = LoginMethod.valueOf(request.getLoginMethod());
                    return loginMethod;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(method -> Mono.fromCallable(() -> {
                    User user = (method == LoginMethod.STANDARD_LOGIN) ? loginStandard(request) : loginWithZk(request);
                    user.setIsLoggedIn(true);
                    userRepository.save(user);
                    String token = jwtUtil.generateAccessToken(user.getUserName());
                    return new LoginResponse(user.getUserName(), "Logged in successfully", token);
                }).subscribeOn(Schedulers.boundedElastic()));
    }

    @Override
    public Mono<LogoutUserResponse> logoutUser(LogoutRequest request) {
        return Mono.fromCallable(() -> {
            User user = userRepository.findUserByUserName(request.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            user.setIsLoggedIn(false);
            userRepository.save(user);
            return new LogoutUserResponse(user.getUserName(), "Logged out successfully");
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public String generateAccessToken(String username) {
        return jwtUtil.generateAccessToken(username);
    }

    @Override
    public boolean validateAccessToken(String token) {
        return jwtUtil.validateToken(token);
    }

    @Override
    public String generateRefreshToken(String userId) {
        return jwtUtil.generateRefreshToken(userId);
    }

    @Override
    public boolean validateRefreshToken(String token) {
        return jwtUtil.validateToken(token);
    }

    @Override
    public Mono<TokenPair> refreshTokens(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken)) {
            return Mono.error(new IllegalArgumentException("Invalid refresh token"));
        }

        String username = jwtUtil.extractUsername(refreshToken);
        String newAccessToken = jwtUtil.generateAccessToken(username);
        String newRefreshToken = jwtUtil.generateRefreshToken(username);
        TokenPair pair = new TokenPair(newAccessToken, newRefreshToken);

        return Mono.just(pair);
    }


    private User createUser(RegisterUserRequest req, String suiAddress) {
        User user = new User();
        user.setUserName(req.getUserName());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole(req.getRole());
        user.setSuiAddress(suiAddress);
        user.setIsLoggedIn(false);
        user.setDateOfCreation(LocalDateTime.now());

        userRepository.save(user);
        return user;
    }

    private RegisterUserResponse registerNewUserResponse(RegisterUserRequest req, User user) {
        RegisterUserResponse response = new RegisterUserResponse();
        response.setUserName(user.getUserName());
        response.setRole(req.getRole());
        response.setIsLoggedIn(false);
        response.setMessage("User registered successfully.");
        return response;
    }

    private User loginStandard(LoginRequest req) {
        validateUserInput(req.getUsername(), req.getPassword());
        User user = userRepository.findUserByUserName(req.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        return user;
    }

    private User loginWithZk(LoginRequest loginRequest) {
        VerifiedAddressResult result = zkLoginService.loginViaZkProof(
                loginRequest.getZkProof(), loginRequest.getPublicKey()
        );
        if (result == null || !result.isSuccess()) {
            throw new IllegalArgumentException("Invalid zk-proof or proof verification failed");
        }
        return (User) userRepository.findUserBySuiAddress(result.getAddress())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private String verifyZkProofAndRegisterOrThrow(
            String zkProof, String userName, String publicKey
    ) {
        String suiAddress = zkLoginService.registerViaZkProof(zkProof, userName, publicKey);
        if (suiAddress == null) {
            throw new IllegalArgumentException("Invalid zk-proof or proof verification failed");
        }
        return suiAddress;
    }


    @Component
    public static class WalletApiClient {
        private final WebClient webClient;

        public WalletApiClient(WebClient.Builder webClientBuilder,
                               @Value("${wallet.api.base:http://localhost:3000}")
                               String baseUrl) {
            this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        }

        public Mono<String> generateSuiAddress() {
            return webClient.post()
                    .uri("/api/wallets")
                    .retrieve()
                    .bodyToMono(WalletResponse.class)
                    .map(WalletResponse::getAddress)
                    .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(2)))
                    .timeout(Duration.ofSeconds(5));
        }

        @Setter
        @Getter
        private static class WalletResponse {
            private String address;
        }
    }
}

