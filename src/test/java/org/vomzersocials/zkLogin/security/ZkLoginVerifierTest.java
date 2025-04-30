package org.vomzersocials.zkLogin.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ZkLoginVerifierTest {

    @Mock
    private SuiZkLoginClient suiZkLoginClient;

    @InjectMocks
    private ZkLoginVerifier zkLoginVerifier;

    @Test
    void testVerifyProof_Success() {
        String zkProof = "valid_base64_proof";
        String publicKey = "valid_public_key";

        when(suiZkLoginClient.verifyProof(zkProof, publicKey))
                .thenReturn(VerifiedAddressResult.success(publicKey));
        boolean isValid = zkLoginVerifier.verifyProof(zkProof, publicKey);
        assertTrue(isValid, "Proof should be valid when response status is OK");
    }

    @Test
    void testVerifyProof_Failure() {
        String zkProof = "invalid_proof";
        String publicKey = "some_public_key";

        when(suiZkLoginClient.verifyProof(zkProof, publicKey))
                .thenReturn(VerifiedAddressResult.failed("error message"));

        boolean isValid = zkLoginVerifier.verifyProof(zkProof, publicKey);
        System.out.println(isValid);
        assertFalse(isValid, "Proof should be invalid when SuiZkLoginClient returns failure");
    }

}

