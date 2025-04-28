package org.vomzersocials.user.services.implementations;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import org.vomzersocials.user.services.interfaces.AuthenticationService;
import org.vomzersocials.zkLogin.security.VerifiedAddressResult;
import org.vomzersocials.zkLogin.services.ZkLoginService;
import org.vomzersocials.user.springSecurity.JwtUtil;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.vomzersocials.user.utils.ValidationUtils.isValidPassword;
import static org.vomzersocials.user.utils.ValidationUtils.isValidUsername;

@Service
@Transactional
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ZkLoginService zkLoginService;
    private final JwtUtil jwtUtil;

    public AuthenticationServiceImpl(UserRepository userRepository,
                                     BCryptPasswordEncoder passwordEncoder,
                                     ZkLoginService zkLoginService,
                                     JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.zkLoginService = zkLoginService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public RegisterUserResponse registerNewUser(RegisterUserRequest request) {
        validateUserInput(request.getUserName(), request.getPassword());
        if (userRepository.findUserByUserName(request.getUserName()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        String suiAddress = verifyZkProofAndRegisterOrThrow(
                request.getZkProof(), request.getUserName(), request.getPublicKey());
        User user = createUser(request, suiAddress);
        return buildRegisterResponse(request, user);
    }

    @Override
    public LoginResponse loginUser(LoginRequest request) {
        LoginMethod method;
        try {
            method = LoginMethod.valueOf(request.getLoginMethod());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid login method", e);
        }

        User user = (method == LoginMethod.STANDARD_LOGIN) ? loginStandard(request) : loginWithZk(request);

        user.setIsLoggedIn(true);
        userRepository.save(user);
        String token = jwtUtil.generateAccessToken(user.getUserName());
        return new LoginResponse(user.getUserName(), "Logged in successfully", token);
    }

    @Override
    public LogoutUserResponse logoutUser(LogoutRequest request) {
        User user = userRepository.findUserByUserName(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setIsLoggedIn(false);
        userRepository.save(user);
        return new LogoutUserResponse(user.getUserName(), "Logged out successfully");
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
    public TokenPair refreshTokens(String refreshToken) {
        if (!validateRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid/expired refresh token");
        }
        String username = jwtUtil.extractUsername(refreshToken);
        return new TokenPair(
                jwtUtil.generateAccessToken(username),
                jwtUtil.generateRefreshToken(username)
        );
    }

    private User createUser(RegisterUserRequest req, String suiAddress) {
        User user = new User();
        user.setUserName(req.getUserName());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole(req.getRole());
        user.setSuiAddress(suiAddress);
        user.setIsLoggedIn(false);
        user.setDateOfCreation(LocalDateTime.now());

        userRepository.save(user);  // save, but ignore returned value
        return user;               // return the original user instance
    }

    private RegisterUserResponse buildRegisterResponse(RegisterUserRequest req, User user) {
        RegisterUserResponse resp = new RegisterUserResponse();
        resp.setUserName(user.getUserName());
        resp.setRole(req.getRole());
        resp.setIsLoggedIn(false);
        resp.setMessage("User registered successfully.");
        return resp;
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

    private User loginWithZk(LoginRequest req) {
        VerifiedAddressResult result = zkLoginService.loginViaZkProof(
                req.getZkProof(), req.getPublicKey()
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

    private void validateUserInput(String username, String password) {
        if (!isValidUsername(username) || !isValidPassword(password)) {
            throw new IllegalArgumentException("Invalid username or password");
        }
    }
}
