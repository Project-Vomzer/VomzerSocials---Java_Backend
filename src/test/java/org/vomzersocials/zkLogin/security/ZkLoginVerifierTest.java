package org.vomzersocials.zkLogin.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ZkLoginVerifierTest {

    @InjectMocks
    private ZkLoginVerifier zkLoginVerifier;

    @Mock
    private RestTemplate restTemplate;

    private static final String SUI_ZKLOGIN_API = "https://fullnode.testnet.sui.io/zklogin/verify";

    @Test
    void testVerifyProof_Success() {
        String zkProof = "valid_base64_proof";
        String publicKey = "valid_public_key";

        // Stub with more specific matchers:
        when(restTemplate.postForEntity(eq(SUI_ZKLOGIN_API), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>("verified", HttpStatus.OK));

        boolean isValid = zkLoginVerifier.verifyProof(zkProof, publicKey);
        assertTrue(isValid, "Proof should be valid when response status is OK");
    }

    @Test
    void testVerifyProof_Failure() {
        String zkProof = "invalid_proof";
        String publicKey = "some_public_key";

        when(restTemplate.postForEntity(eq(SUI_ZKLOGIN_API), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>("not verified", HttpStatus.BAD_REQUEST));

        boolean isValid = zkLoginVerifier.verifyProof(zkProof, publicKey);
        assertFalse(isValid, "Proof should be invalid when response status is not OK");
    }
}

