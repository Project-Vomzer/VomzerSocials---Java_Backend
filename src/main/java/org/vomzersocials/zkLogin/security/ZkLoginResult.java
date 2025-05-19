package org.vomzersocials.zkLogin.security;

import lombok.*;

@Setter
@Getter
//@AllArgsConstructor
//@RequiredArgsConstructor
@ToString
public class ZkLoginResult {
    private final String suiAddress;
    private final String salt;
    private final String publicKey;

    public ZkLoginResult(String suiAddress, String salt, String publicKey) {
        this.suiAddress = suiAddress;
        this.salt = salt;
        this.publicKey = publicKey;
    }
}