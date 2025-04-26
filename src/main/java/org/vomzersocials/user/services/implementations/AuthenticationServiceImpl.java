package org.vomzersocials.user.services.implementations;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
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
import org.vomzersocials.user.enums.LoginMethod;
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

    public AuthenticationServiceImpl(UserRepository userRepository,
                                     BCryptPasswordEncoder passwordEncoder,
                                     ZkLoginService zkLoginService,
                                     SuiZkLoginClient suiZkLoginClient,
                                     JwtUtil jwtUtil) {
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
        LoginMethod loginMethod;
        try {
            // Convert string to enum
            loginMethod = LoginMethod.valueOf(loginRequest.getLoginMethod());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid login method", e);
        }

        User foundUser;
        if (loginMethod == LoginMethod.STANDARD_LOGIN) {
            // Handle standard login
            validateUserInput(loginRequest.getUsername(), loginRequest.getPassword());
            foundUser = userRepository.findUserByUserName(loginRequest.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            if (!passwordEncoder.matches(loginRequest.getPassword(), foundUser.getPassword())) {
                throw new IllegalArgumentException("Invalid credentials");
            }
        } else if (loginMethod == LoginMethod.ZK_LOGIN) {
            // Handle zkLogin
            String suiAddress = verifyZkProofOrThrow(loginRequest.getZkProof(), loginRequest.getPublicKey());
            foundUser = (User) userRepository.findUserBySuiAddress(suiAddress)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
        } else {
            throw new IllegalArgumentException("Unsupported login method");
        }

        foundUser.setIsLoggedIn(true);
        userRepository.save(foundUser);
        String accessToken = jwtUtil.generateAccessToken(foundUser.getUserName());
        return new LoginResponse(foundUser.getUserName(), "Logged in successfully", accessToken);
    }


    @Override
    public LogoutUserResponse logoutUser(LogoutRequest logoutRequest) {
        // 1) Look up the user
        User user = userRepository.findUserByUserName(logoutRequest.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 2) Flip your “isLoggedIn” flag
        user.setIsLoggedIn(false);
        userRepository.save(user);

        // 3) Return a response so the client can drop its tokens
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
