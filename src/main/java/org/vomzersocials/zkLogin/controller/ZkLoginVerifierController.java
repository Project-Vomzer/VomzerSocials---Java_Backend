package org.vomzersocials.zkLogin.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.vomzersocials.zkLogin.dtos.ZkProofRequest;
import org.vomzersocials.zkLogin.security.ZkLoginVerifier;

@RestController
@RequestMapping("/zklogin")
public class ZkLoginVerifierController {

    private final ZkLoginVerifier zkLoginVerifier;

    public ZkLoginVerifierController(ZkLoginVerifier zkLoginVerifier) {
        this.zkLoginVerifier = zkLoginVerifier;
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyProof(@RequestBody ZkProofRequest request) {
        boolean isValid = zkLoginVerifier.verifyProof(request.getZkProof(), request.getPublicKey());
        if (isValid) {
            return ResponseEntity.ok("Proof Verified");
        } else {
            return ResponseEntity.badRequest().body("Invalid Proof");
        }
    }
}
