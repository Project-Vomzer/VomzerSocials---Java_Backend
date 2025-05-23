package org.vomzersocials.zkLogin.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.vomzersocials.zkLogin.dtos.ZkProofRequest;
import org.vomzersocials.zkLogin.security.ZkLoginVerifier;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/zklogin")
public class ZkLoginVerifierController {

    private final ZkLoginVerifier zkLoginVerifier;

    public ZkLoginVerifierController(ZkLoginVerifier zkLoginVerifier) {
        this.zkLoginVerifier = zkLoginVerifier;
    }

    @PostMapping("/verify")
    public Mono<ResponseEntity<String>> verifyProof(@RequestBody ZkProofRequest request) {
        return zkLoginVerifier.verifyProof(request.getZkProof(), request.getPublicKey())
                .map(isValid -> {
                    if (isValid) {
                        return ResponseEntity.ok("Proof Verified");
                    } else {
                        return ResponseEntity.badRequest().body("Invalid Proof");
                    }
                });
    }
}
