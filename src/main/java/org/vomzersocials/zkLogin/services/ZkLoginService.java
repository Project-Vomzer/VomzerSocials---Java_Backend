package org.vomzersocials.zkLogin.services;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.vomzersocials.user.data.models.User;
import org.vomzersocials.user.data.repositories.UserRepository;
import org.vomzersocials.user.dtos.requests.ZkLoginRequest;
import org.vomzersocials.user.enums.Role;
import org.vomzersocials.user.exceptions.UsernameNotFoundException;
import org.vomzersocials.user.exceptions.ZkLoginException;
import org.vomzersocials.zkLogin.security.SuiZkLoginClient;
import org.vomzersocials.zkLogin.security.ZkLoginResult;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;

@Slf4j
@Service
public class ZkLoginService {

    private final SuiZkLoginClient suiZkLoginClient;
    private final UserRepository userRepository;
    private final SecretKey jwtSecretKey;

    @Autowired
    public ZkLoginService(
            SuiZkLoginClient suiZkLoginClient,
            UserRepository userRepository,
            @Value("${jwt.secret}") String jwtSecret
    ) {
        this.suiZkLoginClient = suiZkLoginClient;
        this.userRepository = userRepository;
        this.jwtSecretKey = new SecretKeySpec(
                Base64.getDecoder().decode(jwtSecret),
                "HmacSHA256"
        );
    }

    @Transactional
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public Mono<ZkLoginResult> registerViaZkLogin(String userName, String jwt) {
        log.info("Processing zkLogin registration for user: {}", userName);
        String userId = hashJwtSubject(jwt);
        String salt = generateSalt();

        return findExistingUserById(userId)
                .flatMap(existingUser -> {
                    if (existingUser.isPresent()) {
                        log.error("User already exists for userId: {}", userId);
                        return Mono.error(new IllegalArgumentException("User already exists"));
                    }
                    return generateZkProofAndPublicKey(jwt, salt)
                            .flatMap(zkData -> {
                                String zkProof = zkData[0];
                                String publicKey = zkData[1];

                                return suiZkLoginClient.verifyProof(zkProof, publicKey)
                                        .flatMap(result -> {
                                            if (!result.isSuccess()) {
                                                log.error("Failed to verify zkProof for user: {}", userName);
                                                return Mono.error(new IllegalArgumentException("Failed to verify zkProof: " + result.getErrorMessage()));
                                            }
                                            String suiAddress = result.getAddress();

                                            User user = new User();
                                            user.setId(userId);
                                            user.setUserName(userName);
                                            user.setSuiAddress(suiAddress);
                                            user.setPublicKey(publicKey);
                                            user.setSalt(salt);
                                            user.setRole(Role.USER);
                                            user.setIsLoggedIn(false);

                                            return Mono.fromCallable(() -> {
                                                        User savedUser = userRepository.save(user);
                                                        log.info("Registered user {} with Sui address: {}", userName, suiAddress);
                                                        return savedUser;
                                                    }).subscribeOn(Schedulers.boundedElastic())
                                                    .thenReturn(new ZkLoginResult(suiAddress, publicKey));
                                        });
                            });
                })
                .onErrorMap(e -> {
                    log.error("zkLogin registration failed for user {}: {}", userName, e.getMessage(), e);
                    return new ZkLoginException("zkLogin registration failed: " + e.getMessage(), e);
                });
    }

    @Transactional
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public Mono<String> loginViaZkLogin(ZkLoginRequest request) {
        String jwt = request.getJwt();
        String userId = hashJwtSubject(jwt);
        log.info("Processing zkLogin for userId: {}", userId);

        return findExistingUserById(userId)
                .flatMap(optionalUser -> optionalUser
                        .map(Mono::just)
                        .orElseGet(() -> {
                            log.error("User not found for userId: {}", userId);
                            return Mono.error(new UsernameNotFoundException("User not found for JWT"));
                        }))
                .filter(user -> user.getSalt() != null)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("User salt not found")))
                .flatMap(user -> generateZkProofAndPublicKey(jwt, user.getSalt())
                        .flatMap(zkData -> {
                            String zkProof = zkData[0];
                            String publicKey = zkData[1];

                            return suiZkLoginClient.verifyProof(zkProof, publicKey)
                                    .flatMap(result -> {
                                        if (!result.isSuccess()) {
                                            log.error("Failed to verify zkProof for user: {}", user.getUserName());
                                            return Mono.error(new IllegalArgumentException("Failed to verify zkProof: " + result.getErrorMessage()));
                                        }
                                        String derivedSuiAddress = result.getAddress();
                                        if (!derivedSuiAddress.equals(user.getSuiAddress())) {
                                            log.error(
                                                    "Sui address mismatch for user {}: stored={}, derived={}",
                                                    user.getUserName(),
                                                    user.getSuiAddress(),
                                                    derivedSuiAddress
                                            );
                                            return Mono.error(new IllegalArgumentException("Sui address mismatch"));
                                        }
                                        user.setIsLoggedIn(true);
                                        return Mono.fromCallable(() -> userRepository.save(user))
                                                .subscribeOn(Schedulers.boundedElastic())
                                                .map(savedUser -> {
                                                    log.info("Verified Sui address {} for user {}", derivedSuiAddress, user.getUserName());
                                                    return derivedSuiAddress;
                                                });
                                    });
                        }))
                .onErrorMap(e -> {
                    log.error("zkLogin failed for userId {}: {}", userId, e.getMessage(), e);
                    return new ZkLoginException("zkLogin failed: " + e.getMessage(), e);
                });
    }

    private Mono<Optional<User>> findExistingUserById(String userId) {
        return Mono.fromCallable(() -> {
            log.debug("Searching for user with userId: {}", userId);
            try {
                Optional<User> user = userRepository.findByUserId(userId);
                log.debug("Found user for userId {}: {}", userId, user.isPresent() ? user.get() : "not found");
                return user;
            } catch (Exception e) {
                log.error("Error finding user with userId {}: {}", userId, e.getMessage(), e);
                throw new IllegalArgumentException("Database error while finding user: " + e.getMessage(), e);
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    private Mono<String[]> generateZkProofAndPublicKey(String jwt, String salt) {
        return suiZkLoginClient.generateZkProofAndPublicKey(jwt, salt);
    }

//    @Cacheable(value = "jwtSubjectHashes", key = "#jwt")
    private String hashJwtSubject(String jwt) {
        try {
            String subject = Jwts.parserBuilder()
                    .setSigningKey(jwtSecretKey)
                    .build()
                    .parseClaimsJws(jwt)
                    .getBody()
                    .getSubject();
            if (subject == null) {
                log.error("JWT subject is null");
                throw new JwtException("Invalid JWT: missing subject");
            }
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(subject.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (JwtException e) {
            log.error("Failed to parse JWT: {}", e.getMessage(), e);
            throw new JwtException("Invalid JWT: " + e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm not available: {}", e.getMessage(), e);
            throw new RuntimeException("Hashing error", e);
        }
    }
}