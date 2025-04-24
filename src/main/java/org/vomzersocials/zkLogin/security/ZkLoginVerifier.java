package org.vomzersocials.zkLogin.security;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

@Service
public class ZkLoginVerifier {
    private final RestTemplate restTemplate;

    public ZkLoginVerifier(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean verifyProof(String zkProof, String publicKey) {
        try {
            ResponseEntity<Boolean> response = restTemplate.postForEntity(
                    "https://your-zklogin-verifier-endpoint.com/verify",
                    new ProofRequest(zkProof, publicKey),
                    Boolean.class
            );

            if (response.getStatusCode().is2xxSuccessful() && Boolean.TRUE.equals(response.getBody())) {
                return true;
            } else {
                System.err.println("ZK proof verification failed: " + response);
                return false;
            }
        } catch (RestClientResponseException e) {
            // Log the full response error for debugging
            System.err.println("ZK proof verification error: " + e.getResponseBodyAsString());
            return false;
        } catch (RestClientException e) {
            System.err.println("Request error: " + e.getMessage());
            return false;
        }
    }


    private static class ProofRequest {
        private final String proof;
        private final String publicKey;

        public ProofRequest(String proof, String publicKey) {
            this.proof = proof;
            this.publicKey = publicKey;
        }
    }
}