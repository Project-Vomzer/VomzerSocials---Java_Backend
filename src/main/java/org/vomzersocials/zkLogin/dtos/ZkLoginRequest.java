package org.vomzersocials.zkLogin.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ZkLoginRequest {
    private String zkProof;
    private String publicKey;
}
