package org.vomzersocials.user.services.implementations;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.vomzersocials.user.dtos.responses.*;
import org.vomzersocials.user.services.interfaces.AuthenticationService;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import org.vomzersocials.user.data.models.User;
import org.vomzersocials.user.data.repositories.UserRepository;
import org.vomzersocials.user.dtos.requests.LoginRequest;
import org.vomzersocials.user.dtos.requests.LogoutRequest;
import org.vomzersocials.user.dtos.requests.RegisterUserRequest;
import org.vomzersocials.user.enums.LoginMethod;
import org.vomzersocials.zkLogin.security.VerifiedAddressResult;
import org.vomzersocials.zkLogin.services.ZkLoginService;
import org.vomzersocials.user.springSecurity.JwtUtil;

import java.time.LocalDateTime;
import java.util.List;
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
            log.info("Attempting to register user: {}", request.getUserName());
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
            log.error("Registration failed due to exception: {}", ex.getMessage(), ex);
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
    public Mono<LoginResponse> loginUser(LoginRequest loginRequest) {
        return Mono.fromCallable(() -> LoginMethod.valueOf(loginRequest.getLoginMethod()))
                .doOnNext(loginMethod -> log.info("LoginMethod: {}", loginMethod))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(method ->
                        method == LoginMethod.STANDARD_LOGIN ? handleStandardLogin(loginRequest) : handleZkLogin(loginRequest)
                )
                .flatMap(user -> {
                    user.setIsLoggedIn(true);
                    return Mono.fromCallable(() -> userRepository.save(user))
                            .subscribeOn(Schedulers.boundedElastic())
                            .thenReturn(user);
                })
                .map(user -> {
                    String accessToken = jwtUtil.generateAccessToken(user.getUserName(), List.of(user.getRole().name()));
                    String refreshToken = jwtUtil.generateRefreshToken(user.getUserName());
                    return new LoginResponse(
                            user.getUserName(),
                            "Logged in successfully",
                            accessToken, refreshToken,
                            user.getRole(),
                            loginRequest.getLoginMethod()
                    );
                })
                .doOnNext(resp -> {
                    try {
                        String json = new ObjectMapper()
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(resp);
                        log.info("LoginResponse JSON:\n{}", json);
                    } catch (Exception e) {
                        log.error("Failed to serialize LoginResponse", e);
                    }
                });
    }

    private Mono<User> handleStandardLogin(LoginRequest req) {
        return Mono.<User>fromCallable(() -> {
            log.info("loginStandard() called with username='{}', password='{}'",
                    req.getUsername(), req.getPassword());
            User user = userRepository.findUserByUserName(req.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));
            log.info("Stored hash = {}", user.getPassword());
            boolean matches = passwordEncoder.matches(req.getPassword(), user.getPassword());
            log.info("Password matches? {}", matches);
            if (!matches) throw new IllegalArgumentException("Invalid username or password");
            return user;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private Mono<User> handleZkLogin(LoginRequest req) {
        return Mono.fromCallable(() -> {
            log.info("loginWithZk() called with zkProof='{}', publicKey='{}'",
                    req.getZkProof(), req.getPublicKey());
            VerifiedAddressResult result = zkLoginService.loginViaZkProof(
                    req.getZkProof(), req.getPublicKey()
            );
            if (result == null || !result.isSuccess()) {
                throw new IllegalArgumentException("Invalid zk-proof or proof verification failed");
            }
            String address = result.getAddress();
            log.info("ZK proof succeeded; address = {}", address);
            return (User) userRepository.findUserBySuiAddress(address)
                    .orElseThrow(() -> new IllegalArgumentException("User not found for address " + address));
        }).subscribeOn(Schedulers.boundedElastic());
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
        User user = userRepository.findUserByUserName(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String role = user.getRole().name();
        return jwtUtil.generateAccessToken(username, List.of(role));
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
        return Mono.fromCallable(() -> {
            User user = userRepository.findUserByUserName(username)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            String role = user.getRole().name();
            String newAccessToken = jwtUtil.generateAccessToken(username, List.of(role));
            String newRefreshToken = jwtUtil.generateRefreshToken(username);
            return new TokenPair(newAccessToken, newRefreshToken);
        })
                .subscribeOn(Schedulers.boundedElastic());
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

    private String verifyZkProofAndRegisterOrThrow(
            String zkProof, String userName, String publicKey
    ) {
        String suiAddress = zkLoginService.registerViaZkProof(zkProof, userName, publicKey);
        if (suiAddress == null) {
            throw new IllegalArgumentException("Invalid zk-proof or proof verification failed");
        }
        return suiAddress;
    }

}

