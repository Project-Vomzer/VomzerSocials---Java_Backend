package org.vomzersocials.user.dtos.requests;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.vomzersocials.user.enums.LoginMethod;


@Data
public class ZkLoginRequest {
    @NotEmpty(message = "zkProof cannot be empty")
    private String zkProof;

    @NotEmpty(message = "Public key cannot be empty")
    private String publicKey;

    private LoginMethod loginMethod = LoginMethod.ZK_LOGIN;
}

