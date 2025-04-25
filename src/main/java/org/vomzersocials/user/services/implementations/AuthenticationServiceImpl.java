package org.vomzersocials.user.services.implementations;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.vomzersocials.user.data.models.User;
import org.vomzersocials.user.data.repositories.UserRepository;
import org.vomzersocials.user.dtos.requests.LoginRequest;
import org.vomzersocials.user.dtos.requests.LogoutRequest;
import org.vomzersocials.user.dtos.responses.LogoutUserResponse;
import org.vomzersocials.user.dtos.requests.RegisterUserRequest;
import org.vomzersocials.user.dtos.responses.LoginResponse;
import org.vomzersocials.user.dtos.responses.RegisterUserResponse;
import org.vomzersocials.user.dtos.responses.TokenPair;
import org.vomzersocials.user.services.interfaces.AuthenticationService;
import org.vomzersocials.zkLogin.security.SuiZkLoginClient;
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
    private final SuiZkLoginClient suiZkLoginClient;
    private final JwtUtil jwtUtil;

    public AuthenticationServiceImpl(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, ZkLoginService zkLoginService, SuiZkLoginClient suiZkLoginClient, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.zkLoginService = zkLoginService;
        this.suiZkLoginClient = suiZkLoginClient;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public RegisterUserResponse registerNewUser(RegisterUserRequest registerUserRequest) {
        validateUserInput(registerUserRequest.getUserName(), registerUserRequest.getPassword());

        Optional<User> existingUser = userRepository.findUserByUserName(registerUserRequest.getUserName());
        if (existingUser.isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        String suiAddress = verifyZkProofAndRegisterOrThrow(registerUserRequest.getZkProof(), registerUserRequest.getUserName(), registerUserRequest.getPublicKey());
        User user = getUserDetails(registerUserRequest, suiAddress);
        return getRegisterUserResponse(registerUserRequest, user);
//        return new RegisterUserResponse(user.getUserName(), false, "User registered successfully.");
    }

    private static RegisterUserResponse getRegisterUserResponse(RegisterUserRequest registerUserRequest, User user) {
        RegisterUserResponse registerUserResponse = new RegisterUserResponse();
        registerUserResponse.setUserName(user.getUserName());
        registerUserResponse.setRole(registerUserRequest.getRole());
        registerUserResponse.setIsLoggedIn(false);
        registerUserResponse.setMessage("User registered successfully.");
        return registerUserResponse;
    }

    private User getUserDetails(RegisterUserRequest registerUserRequest, String suiAddress) {
        User user = new User();
        user.setUserName(registerUserRequest.getUserName());
        user.setPassword(passwordEncoder.encode(registerUserRequest.getPassword()));
        user.setRole(registerUserRequest.getRole());
        user.setSuiAddress(suiAddress);
        user.setIsLoggedIn(false);
        user.setDateOfCreation(LocalDateTime.now());

        userRepository.save(user);
        return user;
    }


    @Override
    public LoginResponse loginUser(LoginRequest loginRequest) {
        validateUserInput(loginRequest.getUsername(), loginRequest.getPassword());

        String suiAddress = verifyZkProofOrThrow(loginRequest.getZkProof(), loginRequest.getPublicKey());
        User foundUser = (User) userRepository.findUserBySuiAddress(suiAddress)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        foundUser.setIsLoggedIn(true);
        userRepository.save(foundUser);
        String accessToken = jwtUtil.generateAccessToken(foundUser.getUserName());
        return new LoginResponse(foundUser.getUserName(), "Logged in successfully", accessToken);
    }


    @Override
    public LogoutUserResponse logoutUser(LogoutRequest logoutRequest) {
        User user = userRepository.findUserByUserName(logoutRequest.getUserName())
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
        String userId = jwtUtil.extractUsername(refreshToken);
        return new TokenPair(jwtUtil.generateAccessToken(userId), jwtUtil.generateRefreshToken(userId));
    }

    private void validateUserInput(String username, String password) {
        if (!isValidUsername(username) || !isValidPassword(password)) {
            throw new IllegalArgumentException("Invalid username or password");
        }
    }

    private String verifyZkProofAndRegisterOrThrow(String zkProof, String userName, String publicKey) {
        String suiAddress = zkLoginService.registerViaZkProof(zkProof, userName, publicKey);
        if (suiAddress == null) throw new IllegalArgumentException("Invalid zk-proof or proof verification failed");
        return suiAddress;
    }

    private String verifyZkProofOrThrow(String zkProof, String publicKey) {
        String suiAddress = zkLoginService.loginViaZkProof(zkProof, publicKey);
        if (suiAddress == null) throw new IllegalArgumentException("Invalid zk-proof or proof verification failed");
        return suiAddress;
    }


}
