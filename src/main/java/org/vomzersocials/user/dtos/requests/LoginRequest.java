package org.vomzersocials.user.dtos.requests;

import lombok.*;
import org.vomzersocials.user.enums.Role;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LoginRequest {
    private String username;
    private String password;
    private String zkProof;
    private String publicKey;
}
