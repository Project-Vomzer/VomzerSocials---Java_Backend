package org.vomzersocials.zkLogin.security;

import org.springframework.stereotype.Component;

@Component
public class ZkLoginVerifier {

    private final SuiZkLoginClient suiZkLoginClient;

    public ZkLoginVerifier(SuiZkLoginClient suiZkLoginClient) {
        this.suiZkLoginClient = suiZkLoginClient;
    }

    public boolean verifyProof(String zkProof, String publicKey) {
//        String suiAddress = String.valueOf(suiZkLoginClient.verifyProof(zkProof, publicKey));
//        return suiAddress != null;
        VerifiedAddressResult addressResult = suiZkLoginClient.verifyProof(zkProof, publicKey);
        return addressResult != null && addressResult.isSuccess();
    }

}
