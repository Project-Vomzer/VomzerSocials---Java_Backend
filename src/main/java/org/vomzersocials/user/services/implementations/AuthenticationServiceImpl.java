package org.vomzersocials.user.services.implementations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.vomzersocials.user.dtos.responses.*;
import org.vomzersocials.user.services.interfaces.AuthenticationService;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import org.vomzersocials.user.data.models.User;
import org.vomzersocials.user.data.repositories.UserRepository;
import org.vomzersocials.user.dtos.requests.*;
import org.vomzersocials.zkLogin.services.ZkLoginService;
import org.vomzersocials.user.springSecurity.JwtUtil;
import org.vomzersocials.user.enums.Role;

import java.util.List;
import java.util.Optional;

import static org.vomzersocials.user.utils.ValidationUtils.isValidPassword;
import static org.vomzersocials.user.utils.ValidationUtils.isValidUsername;

@Service
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ZkLoginService zkLoginService;
    private final WalletApiClient walletApiClient;
    private final JwtUtil jwtUtil;

    public AuthenticationServiceImpl(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder,
                                     ZkLoginService zkLoginService, WalletApiClient walletApiClient, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.zkLoginService = zkLoginService;
        this.walletApiClient = walletApiClient;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<RegisterUserResponse> registerWithStandardLogin(StandardRegisterRequest request) {
        log.info("Attempting standard registration for user: {}", request.getUserName());
        validateUserInput(request.getUserName(), request.getPassword());
        return findExistingUser(request.getUserName())
                .flatMap(existingUser -> {
                    if (existingUser.isPresent()) {
                        return Mono.error(new IllegalArgumentException("Username already exists"));
                    }
                    return walletApiClient.generateSuiAddress()
                            .flatMap(address -> createUser(request.getUserName(), request.getPassword(), address, null, Role.USER)
                                    .map(user -> registerNewUserResponse(request.getUserName(), user)));
                })
                .onErrorMap(IllegalArgumentException.class, e -> new IllegalArgumentException("Registration failed: " + e.getMessage()));
    }

    @Override
    public Mono<RegisterUserResponse> registerWithZkLogin(ZkRegisterRequest request) {
        log.info("Attempting zkLogin registration for user: {}", request.getUserName());
        validateUserInput(request.getUserName(), null);
        return findExistingUser(request.getUserName())
                .flatMap(existingUser -> {
                    if (existingUser.isPresent()) {
                        return Mono.error(new IllegalArgumentException("Username already exists"));
                    }
                    return zkLoginService.registerViaZkLogin(request.getUserName(), request.getJwt())
                            .flatMap(result -> createUser(
                                    request.getUserName(),
                                    null,
                                    result.getSuiAddress(),
                                    result.getPublicKey(),
                                    Role.USER)
                                    .map(user -> registerNewUserResponse(request.getUserName(), user)));
                })
                .onErrorMap(IllegalArgumentException.class, e ->
                        new IllegalArgumentException("zkRegistration failed: " + e.getMessage()));
    }

    @Override
    public Mono<RegisterUserResponse> registerAdmin(StandardRegisterRequest request) {
        log.info("Attempting admin registration for user: {}", request.getUserName());
        validateUserInput(request.getUserName(), request.getPassword());
        return findExistingUser(request.getUserName())
                .flatMap(existingUser -> {
                    if (existingUser.isPresent()) {
                        return Mono.error(new IllegalArgumentException("Username already exists"));
                    }
                    return walletApiClient.generateSuiAddress()
                            .flatMap(address -> createUser(request.getUserName(), request.getPassword(), address, null, Role.ZKSocials)
                                    .map(user -> registerNewUserResponse(request.getUserName(), user)));
                })
                .onErrorMap(IllegalArgumentException.class, e -> new IllegalArgumentException("Admin registration failed: " + e.getMessage()));
    }

    @Override
    public Mono<LoginResponse> loginWithStandardLogin(StandardLoginRequest request) {
        log.info("Attempting standard login for user: {}", request.getUserName());
        return handleStandardLogin(request)
                .flatMap(user -> Mono.fromCallable(() -> {
                            user.setIsLoggedIn(true);
                            return userRepository.save(user);
                        }).subscribeOn(Schedulers.boundedElastic())
                        .thenReturn(user))
                .map(user -> createLoginResponse(user, request.getLoginMethod().name()))
                .doOnNext(this::logLoginResponse)
                .onErrorMap(IllegalArgumentException.class, e -> new IllegalArgumentException("Login failed: " + e.getMessage()));
    }

    @Override
    public Mono<LoginResponse> loginWithZkLogin(ZkLoginRequest request) {
        log.info("Attempting zkLogin with JWT");
        return zkLoginService.loginViaZkLogin(request)
                .flatMap(suiAddress -> Mono.fromCallable(() -> {
                    User user = userRepository.findUserBySuiAddress(suiAddress)
                            .orElseThrow(() -> new IllegalArgumentException("User not found for address " + suiAddress));
                    user.setIsLoggedIn(true);
                    return userRepository.save(user);
                }).subscribeOn(Schedulers.boundedElastic()))
                .map(user -> createLoginResponse(user, "ZK_LOGIN"))
                .doOnNext(this::logLoginResponse)
                .onErrorMap(IllegalArgumentException.class, e ->
                        new IllegalArgumentException("zkLogin failed: " + e.getMessage()));
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
    public Mono<String> generateAccessToken(String username) {
        return Mono.fromCallable(() -> userRepository.findUserByUserName(username)
                        .orElseThrow(() -> new IllegalArgumentException("User not found")))
                .subscribeOn(Schedulers.boundedElastic())
                .map(user -> jwtUtil.generateAccessToken(username, List.of(user.getRole().name())));
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
        String username = jwtUtil.extractUsername(refreshToken);
        return Mono.fromCallable(() -> {
            User user = userRepository.findUserByUserName(username)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            String newAccessToken = jwtUtil.generateAccessToken(username, List.of(user.getRole().name()));
            String newRefreshToken = jwtUtil.generateRefreshToken(username);
            return new TokenPair(newAccessToken, newRefreshToken);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private void validateUserInput(String username, String password) {
        boolean isZkLogin = password == null;
        if (!isValidUsername(username) || (!isZkLogin && !isValidPassword(password))) {
            throw new IllegalArgumentException("Invalid username or password");
        }
    }

    private Mono<Optional<User>> findExistingUser(String userName) {
        return Mono.fromCallable(() -> userRepository.findUserByUserName(userName))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private Mono<User> handleStandardLogin(StandardLoginRequest req) {
        return Mono.fromCallable(() -> {
            log.info("Standard login with username='{}'", req.getUserName());
            User user = userRepository.findUserByUserName(req.getUserName())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));
            boolean matches = passwordEncoder.matches(req.getPassword(), user.getPassword());
            if (!matches) throw new IllegalArgumentException("Invalid username or password");
            return user;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private LoginResponse createLoginResponse(User user, String loginMethod) {
        String accessToken = jwtUtil.generateAccessToken(user.getUserName(), List.of(user.getRole().name()));
        String refreshToken = jwtUtil.generateRefreshToken(user.getUserName());
        return new LoginResponse(
                user.getUserName(),
                "Logged in successfully",
                accessToken,
                refreshToken,
                user.getRole(),
                loginMethod
        );
    }

    private void logLoginResponse(LoginResponse resp) {
        try {
            String json = new ObjectMapper()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(resp);
            log.info("LoginResponse JSON:\n{}", json);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize LoginResponse", e);
        }
    }

    private Mono<User> createUser(String username, String password, String suiAddress, String publicKey, Role role) {
        return Mono.fromCallable(() -> {
            User user = new User();
            user.setUserName(username);
            if (password != null) {
                user.setPassword(passwordEncoder.encode(password));
            }
            user.setRole(role != null ? role : Role.USER);
            user.setSuiAddress(suiAddress);
            user.setPublicKey(publicKey);
            user.setIsLoggedIn(false);
            return userRepository.save(user);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private RegisterUserResponse registerNewUserResponse(String username, User user) {
        RegisterUserResponse response = new RegisterUserResponse();
        response.setUserName(user.getUserName());
        response.setIsLoggedIn(false);
        response.setMessage("User registered successfully.");
        return response;
    }

    private Mono<String> verifyZkProofAndRegisterOrThrow(String zkProof, String userName, String publicKey) {
        log.info("Verifying zkProof: {}, publicKey: {}", zkProof, publicKey);
        String suiAddress = zkLoginService.registerViaZkProof(zkProof, userName, publicKey);
        boolean isValidZkProof = Boolean.TRUE.equals(zkLoginService.isValidZkProof(zkProof, publicKey).block());

        if (!isValidZkProof) {
            return Mono.error(new IllegalArgumentException("Invalid zkProof"));
        }
        return Mono.just(suiAddress);
    }
}