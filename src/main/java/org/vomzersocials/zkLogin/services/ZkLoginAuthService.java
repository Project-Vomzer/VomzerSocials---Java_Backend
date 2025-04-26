package org.vomzersocials.zkLogin.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vomzersocials.zkLogin.dtos.ZkLoginRequest;
import org.vomzersocials.user.data.models.User;
import org.vomzersocials.user.data.repositories.UserRepository;
import org.vomzersocials.user.springSecurity.JwtUtil;

@Service
public class ZkLoginAuthService {

    private final ZkLoginService zkLoginService;
    private final UserRepository userRepository;
    @Autowired
    private JwtUtil jwtUtil;

    public ZkLoginAuthService(ZkLoginService zkLoginService, UserRepository userRepository, JwtUtil jwtUtil) {
        this.zkLoginService = zkLoginService;
        this.userRepository = userRepository;
        this.jwtUtil       = jwtUtil;
    }

    /**
     * 1️⃣ Verify zk-proof → get Sui address (or null)
     * 2️⃣ Find user by that address
     * 3️⃣ Generate and return a JWT for the user
     */
    public String authenticate(ZkLoginRequest req) {
        if (req == null) {
            throw new IllegalArgumentException("Login request cannot be null");
        }

        String suiAddress = zkLoginService.loginViaZkProof(req.getZkProof(), req.getPublicKey());
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
