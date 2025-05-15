package org.vomzersocials.zkLogin.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.vomzersocials.user.data.repositories.UserRepository;
import org.vomzersocials.user.dtos.requests.ZkLoginRequest;
import org.vomzersocials.user.springSecurity.JwtUtil;
import org.vomzersocials.zkLogin.security.ZkLoginVerifier;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Service
@Slf4j
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
                    return Mono.fromCallable(() -> userRepository.findByPublicKey(req.getPublicKey())
                                    .orElseThrow(() -> new IllegalArgumentException("User not found")))
                            .subscribeOn(Schedulers.boundedElastic())
                            .map(user -> jwtUtil.generateAccessToken(user.getUserName(), List.of(user.getRole().name())));
                });
    }
}