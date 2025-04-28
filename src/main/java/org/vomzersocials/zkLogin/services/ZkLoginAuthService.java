package org.vomzersocials.zkLogin.services;

import org.springframework.stereotype.Service;
import org.vomzersocials.zkLogin.dtos.ZkLoginRequest;
import org.vomzersocials.user.data.models.User;
import org.vomzersocials.user.data.repositories.UserRepository;
import org.vomzersocials.user.springSecurity.JwtUtil;

@Service
public class ZkLoginAuthService {

    private final ZkLoginService zkLoginService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public ZkLoginAuthService(ZkLoginService zkLoginService, UserRepository userRepository, JwtUtil jwtUtil) {
        this.zkLoginService = zkLoginService;
        this.userRepository = userRepository;
        this.jwtUtil       = jwtUtil;
    }

    public String authenticate(ZkLoginRequest req) {
        if (req == null) {
            throw new IllegalArgumentException("Login request cannot be null");
        }

        String suiAddress = String.valueOf(zkLoginService.loginViaZkProof(req.getZkProof(), req.getPublicKey()));
        if (suiAddress == null) {
            throw new IllegalArgumentException("Invalid zero-knowledge proof");
        }

        User user = (User) userRepository
                .findUserBySuiAddress(suiAddress)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return jwtUtil.generateAccessToken(user.getUserName());
    }

    public String authenticateUser(ZkLoginRequest request) {
            return authenticate(request);
    }
}
