package org.vomzersocials.zkLogin.security;

import org.springframework.stereotype.Service;
import org.vomzersocials.user.data.models.User;
import org.vomzersocials.user.data.repositories.UserRepository;
import org.vomzersocials.zkLogin.dtos.ZkLoginRequest;

import javax.naming.AuthenticationException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final ZkLoginVerifier zkLoginVerifier;

    public AuthService(UserRepository userRepository, ZkLoginVerifier zkLoginVerifier) {
        this.userRepository = userRepository;
        this.zkLoginVerifier = zkLoginVerifier;
    }

    public String authenticateUser(ZkLoginRequest request) throws AuthenticationException {
        if (request == null) {
            throw new IllegalArgumentException("Login request cannot be null");
        }

        String zkProof = request.getZkProof();
        String publicKey = request.getPublicKey();

        if (!zkLoginVerifier.verifyProof(zkProof, publicKey)) {
            throw new AuthenticationException("Invalid zero-knowledge proof");
        }

        User user = userRepository.findByPublicKey(publicKey)
                .orElseThrow(() -> new AuthenticationException("User not found"));

        return JwtUtil.generateAccessToken(user.getId());
    }
}