package org.vomzersocials.zkLogin.security;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ZkLoginVerifier {

    private final SuiZkLoginClient suiZkLoginClient;

    public ZkLoginVerifier(SuiZkLoginClient suiZkLoginClient) {
        this.suiZkLoginClient = suiZkLoginClient;
    }

    public Mono<Boolean> verifyProof(String zkProof, String publicKey) {
        return suiZkLoginClient.verifyProof(zkProof, publicKey)
                .map(result -> result != null && result.isSuccess())
                .onErrorReturn(false);
    }
}

