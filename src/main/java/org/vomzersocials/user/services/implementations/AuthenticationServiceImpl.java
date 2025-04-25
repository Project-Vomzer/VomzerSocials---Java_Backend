package org.vomzersocials.user.services.implementations;

import org.springframework.beans.factory.annotation.Autowired;
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
import org.vomzersocials.zkLogin.security.SuiZkLoginClient;
import org.vomzersocials.zkLogin.services.ZkLoginService;
import org.vomzersocials.user.springSecurity.JwtUtil;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@Transactional
public class AuthenticationServiceImpl implements org.vomzersocials.user.services.interfaces.AuthenticationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private ZkLoginService zkLoginService;

    @Autowired
    private SuiZkLoginClient suiZkLoginClient;

    @Autowired
    private JwtUtil jwtUtil;  // Inject JwtUtil

    @Override
    public RegisterUserResponse registerNewUser(RegisterUserRequest registerUserRequest) {
        validateUserInput(registerUserRequest.getUserName(), registerUserRequest.getPassword());

        // Check if the user already exists
        Optional<User> existingUser = userRepository.findUserByUserName(registerUserRequest.getUserName());
        if (existingUser.isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        User user = new User();
        user.setUserName(registerUserRequest.getUserName());
        user.setPassword(passwordEncoder.encode(registerUserRequest.getPassword()));
        user.setRole(registerUserRequest.getRole());
        user.setIsLoggedIn(false);
        user.setDateOfCreation(LocalDateTime.now());

        userRepository.save(user);

        return new RegisterUserResponse(user.getUserName(), false, "User registered successfully.");
    }

    @Override
    public LoginResponse loginUser(LoginRequest loginRequest) {
        validateUserInput(loginRequest.getUsername(), loginRequest.getPassword());

        User foundUser = userRepository.findUserByUserName(loginRequest.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), foundUser.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        foundUser.setIsLoggedIn(true);
        userRepository.save(foundUser);

        // Generate the JWT token
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
        return new TokenPair(
                jwtUtil.generateAccessToken(userId),
                jwtUtil.generateRefreshToken(userId)
        );
    }

    private void validateUserInput(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Username and password are required");
        }

        if (Pattern.compile("\\s").matcher(username).find() || Pattern.compile("\\s").matcher(password).find()) {
            throw new IllegalArgumentException("Username and password cannot contain spaces");
        }
    }
}
