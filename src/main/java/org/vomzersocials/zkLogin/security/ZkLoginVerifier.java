package org.vomzersocials.zkLogin.security;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ZkLoginVerifier {
    private final RestTemplate restTemplate;

    // Constructor injection
    public ZkLoginVerifier(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean verifyProof(String zkProof, String publicKey) {
        // Your logic to call the verification endpoint
        return restTemplate.postForEntity(
                "https://your-zklogin-verifier-endpoint.com/verify",
                new ProofRequest(zkProof, publicKey),
                Boolean.class
        ).getBody();
    }

    // Inner class for the request body
    private static class ProofRequest {
        private final String proof;
        private final String publicKey;

        public ProofRequest(String proof, String publicKey) {
            this.proof = proof;
            this.publicKey = publicKey;
        }
    }
}