package org.vomzersocials.zkLogin.security;

import org.springframework.security.authentication.BadCredentialsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vomzersocials.user.data.models.User;
import org.vomzersocials.user.data.repositories.UserRepository;
import org.vomzersocials.zkLogin.dtos.ZkLoginRequest;

@Service
@Slf4j
public class AuthService {
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final ZkLoginVerifier zkLoginVerifier;

    @Autowired
    public AuthService(JwtUtil jwtUtil, UserRepository userRepository, ZkLoginVerifier zkLoginVerifier) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.zkLoginVerifier = zkLoginVerifier;
    }

    public String authenticateUser(ZkLoginRequest zkLoginRequest) {
        boolean isValid = zkLoginVerifier.verifyProof(zkLoginRequest.getZkProof(), zkLoginRequest.getPublicKey());
        log.info("ZK proof validation result: {}", isValid);

        if (!isValid) {
            throw new BadCredentialsException("Invalid ZK proof");
        }

        User user = userRepository.findByPublicKey(zkLoginRequest.getPublicKey())
                .orElseGet(() -> registerNewUser(zkLoginRequest.getPublicKey()));

        return jwtUtil.generateAccessToken(user.getId());
    }

    private User registerNewUser(String publicKey) {
        User user = new User();
        user.setPublicKey(publicKey);
        return userRepository.save(user);
    }
}
