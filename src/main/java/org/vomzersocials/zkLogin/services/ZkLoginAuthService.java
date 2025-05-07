package org.vomzersocials.zkLogin.services;

import org.springframework.stereotype.Service;
import org.vomzersocials.zkLogin.dtos.ZkLoginRequest;
import org.vomzersocials.user.data.models.User;
import org.vomzersocials.user.data.repositories.UserRepository;
import org.vomzersocials.user.springSecurity.JwtUtil;
import org.vomzersocials.zkLogin.security.ZkLoginVerifier;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Service
public class ZkLoginAuthService {

    private final ZkLoginVerifier zkLoginVerifier;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public ZkLoginAuthService(UserRepository userRepository, JwtUtil jwtUtil, ZkLoginVerifier zkLoginVerifier) {
        this.zkLoginVerifier = zkLoginVerifier;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    public Mono<String> authenticate(ZkLoginRequest req) {
        if (req == null) {
            return Mono.error(new IllegalArgumentException("Login request cannot be null"));
        }

        return zkLoginVerifier.verifyProof(req.getZkProof(), req.getPublicKey())
                .flatMap(valid -> {
                    if (!valid) {
                        return Mono.error(new IllegalArgumentException("Invalid zkLogin proof"));
                    }

                    return Mono.defer(() -> {
                        User user = userRepository.findByPublicKey(req.getPublicKey())
                                .orElseThrow(() -> new IllegalArgumentException("User not found"));
                        String accessToken = jwtUtil.generateAccessToken(user.getUserName(), List.of(user.getRole().name()));
                        return Mono.just(accessToken);
                    });
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<String> authenticateUser(ZkLoginRequest request) {
        return authenticate(request);
    }
}
