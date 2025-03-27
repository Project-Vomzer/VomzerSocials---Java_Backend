package org.vomzersocials.zkLogin.security;

import javax.naming.AuthenticationException; // Corrected import

import lombok.extern.slf4j.Slf4j;
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

    public AuthService(JwtUtil jwtUtil, UserRepository userRepository, ZkLoginVerifier zkLoginVerifier) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.zkLoginVerifier = zkLoginVerifier;
    }

    public String authenticateUser(ZkLoginRequest zkLoginRequest) throws AuthenticationException {
        boolean isValid = zkLoginVerifier.verifyProof(zkLoginRequest.getZkProof(), zkLoginRequest.getPublicKey());
        log.info("Is valid is "+isValid);


        if (!isValid) { // Fixed the condition
            throw new AuthenticationException("Invalid ZK proof");
        }

        User user = userRepository.findByPublicKey(zkLoginRequest.getPublicKey())
                .orElseGet(() -> registerNewUser(zkLoginRequest.getPublicKey()));

        return JwtUtil.generateAccessToken(user.getId().toString());
    }



    private User registerNewUser(String publicKey) {
        User user = new User();
        user.setPublicKey(publicKey);
        return userRepository.save(user);
    }
}
