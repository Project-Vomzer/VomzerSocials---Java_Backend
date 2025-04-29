package org.vomzersocials.zkLogin.security;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ZkLoginVerifier {

    @Setter
    @Autowired
    private RestTemplate restTemplate;
    private final SuiZkLoginClient suiZkLoginClient;

    public ZkLoginVerifier(RestTemplate restTemplate, SuiZkLoginClient suiZkLoginClient) {
        this.restTemplate = restTemplate;
        this.suiZkLoginClient = suiZkLoginClient;
    }

    public boolean verifyProof(String zkProof, String publicKey) {
        String suiAddress = String.valueOf(suiZkLoginClient.verifyProof(zkProof, publicKey));
        return suiAddress != null;
    }

}
