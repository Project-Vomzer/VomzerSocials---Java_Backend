package org.vomzersocials.zkLogin.services;

import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vomzersocials.user.data.models.User;
import org.vomzersocials.user.data.repositories.UserRepository;
import org.vomzersocials.user.dtos.requests.ZkLoginRequest;
import org.vomzersocials.user.enums.Role;
import org.vomzersocials.zkLogin.security.SuiZkLoginClient;
import org.vomzersocials.zkLogin.security.VerifiedAddressResult;
import org.vomzersocials.zkLogin.security.ZkLoginResult;
import reactor.core.publisher.Mono;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
@Service
public class ZkLoginService {

    private final SuiZkLoginClient suiZkLoginClient;
    private final UserRepository userRepository;

    @Autowired
    public ZkLoginService(SuiZkLoginClient suiZkLoginClient, UserRepository userRepository) {
        this.suiZkLoginClient = suiZkLoginClient;
        this.userRepository = userRepository;
    }

    public Mono<ZkLoginResult> registerViaZkLogin(String userName, String jwt) {
        log.info("Processing zkLogin registration for user: {}", userName);

        String salt = generateSalt();
        String jwtSubjectHash = hashJwtSubject(jwt);
        return generateZkProofAndPublicKey(jwt, salt)
                .flatMap(zkData -> {
                    String zkProof = zkData[0];
                    String publicKey = zkData[1];

                    if (!isValidZkProof(zkProof, publicKey)) {
                        return Mono.error(new IllegalArgumentException("Invalid zkProof"));
                    }

                    VerifiedAddressResult result = suiZkLoginClient.verifyProof(zkProof, publicKey);
                    if (result == null || !result.isSuccess()) {
                        return Mono.error(new IllegalArgumentException("Failed to verify zkProof"));
                    }

                    String suiAddress = result.getAddress();

                    User user = new User();
                    user.setUserName(userName);
                    user.setSuiAddress(suiAddress);
                    user.setPublicKey(publicKey);
                    user.setSalt(salt);
                    user.setJwtSubjectHash(jwtSubjectHash);
                    user.setRole(Role.USER);
                    user.setIsLoggedIn(false);
                    userRepository.save(user);

                    log.info("Registered user {} with Sui address: {}", userName, suiAddress);
                    return Mono.just(new ZkLoginResult(suiAddress, publicKey));
                });
    }

    public Mono<String> loginViaZkLogin(ZkLoginRequest request) {
        String jwt = request.getJwt();
        String jwtSubjectHash = hashJwtSubject(jwt);
        log.info("Processing zkLogin with JWT for subject hash: {}", jwtSubjectHash);
        return Mono.fromCallable(() -> userRepository.findByJwtSubjectHash(jwtSubjectHash))
                .flatMap(optionalUser -> optionalUser
                        .map(Mono::just)
                        .orElseGet(() -> Mono.error(new IllegalArgumentException("User not found for JWT"))))
                .filter(user -> user.getSalt() != null)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("User salt not found")))
                .flatMap(user -> generateZkProofAndPublicKey(jwt, user.getSalt())
                        .flatMap(zkData -> {
                            String zkProof = zkData[0];
                            String publicKey = zkData[1];

                            if (!isValidZkProof(zkProof, publicKey)) {
                                return Mono.error(new IllegalArgumentException("Invalid zkProof"));
                            }

                            VerifiedAddressResult result = suiZkLoginClient.verifyProof(zkProof, publicKey);
                            if (result == null || !result.isSuccess()) {
                                return Mono.error(new IllegalArgumentException("Failed to verify zkProof"));
                            }

                            String derivedSuiAddress = result.getAddress();
                            if (derivedSuiAddress.equals(user.getSuiAddress())) {
                                log.info("Verified Sui address {} for user {}", derivedSuiAddress, user.getUserName());
                                return Mono.just(derivedSuiAddress);
                            }
                            return Mono.error(new IllegalArgumentException("Sui address mismatch"));
                        }));
    }

    public boolean isValidZkProof(String zkProof, String publicKey) {
        log.info("Verifying zkProof: {}, publicKey: {}", zkProof, publicKey);
        try {
            if (zkProof == null || zkProof.isEmpty()) {
                log.error("zkProof is null or empty");
                return false;
            }
            if (publicKey == null || publicKey.isEmpty() || !publicKey.startsWith("0x")) {
                log.error("Invalid publicKey format: {}", publicKey);
                return false;
            }

            VerifiedAddressResult result = suiZkLoginClient.verifyProof(zkProof, publicKey);
            if (result == null || !result.isSuccess()) {
                log.error("zkProof verification failed for publicKey: {}", publicKey);
                return false;
            }

            String suiAddress = result.getAddress();
            if (suiAddress == null || !suiAddress.startsWith("0x") || suiAddress.length() != 66) {
                log.error("Invalid Sui address format: {}", suiAddress);
                return false;
            }

            log.info("zkProof verified successfully for publicKey: {}, Sui address: {}", publicKey, suiAddress);
            return true;
        } catch (Exception e) {
            log.error("Error verifying zkProof: {}", e.getMessage(), e);
            return false;
        }
    }

    public String registerViaZkProof(String zkProof, String userName, String publicKey) {
        log.info("Registering user {} with zkProof and publicKey: {}", userName, publicKey);
        VerifiedAddressResult result = suiZkLoginClient.verifyProof(zkProof, publicKey);
        if (result == null || !result.isSuccess()) {
            log.error("zkProof verification failed for user: {}", userName);
            throw new IllegalArgumentException("Invalid zk-proof or proof verification failed");
        }
        String suiAddress = result.getAddress();
        User user = userRepository.findUserByUserName(userName)
                .orElseThrow(() -> new IllegalArgumentException("User not found for zk-registration"));
        user.setSuiAddress(suiAddress);
        user.setPublicKey(publicKey);
        userRepository.save(user);
        log.info("Generated Sui address: {} for user: {}", suiAddress, userName);
        return suiAddress;
    }

    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    private Mono<String[]> generateZkProofAndPublicKey(String jwt, String salt) {
        return Mono.fromCallable(() -> {
            String[] result = suiZkLoginClient.generateZkProofAndPublicKey(jwt, salt);
            if (result == null || result.length != 2) {
                throw new IllegalArgumentException("Failed to generate zkProof and publicKey");
            }
            return result;
        });
    }

    private String hashJwtSubject(String jwt) {
        try {
            String subject = Jwts.parser().setSigningKey("your-secret-key").parseClaimsJws(jwt).getBody().getSubject();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(subject.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            log.error("Failed to hash JWT subject: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Invalid JWT");
        }
    }
}