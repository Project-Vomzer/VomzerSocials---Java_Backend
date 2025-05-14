package org.vomzersocials.user.dtos.requests;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;


@Data
public class ZkRegisterRequest {
    @NotEmpty(message = "Username cannot be empty")
    private String userName;

    @NotEmpty(message = "zkProof cannot be empty")
    private String zkProof;

    @NotEmpty(message = "Public key cannot be empty")
    private String publicKey;
}
