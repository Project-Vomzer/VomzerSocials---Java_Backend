package org.vomzersocials.zkLogin.security;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@RequiredArgsConstructor
@ToString
public class ZkLoginResult {
    private String suiAddress;
    private String publicKey;
}