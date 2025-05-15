package org.vomzersocials.zkLogin.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SuiZkLoginClient {

    public VerifiedAddressResult verifyProof(String zkProof, String publicKey) {
        VerifiedAddressResult result = new VerifiedAddressResult();
        try {
            result.setSuccess(true);
            result.setAddress("0x" + publicKey.substring(2, 66));
            return result;
        } catch (Exception e) {
            log.error("Error verifying zkProof: {}", e.getMessage(), e);
            result.setSuccess(false);
            return result;
        }
    }

    public String[] generateZkProofAndPublicKey(String jwt, String salt) {
        String publicKey = "0x" + hash(jwt + salt);
        String zkProof = "proof_" + hash(jwt + salt);
        return new String[]{zkProof, publicKey};
    }

    private String hash(String input) {
        return "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef";
    }
}