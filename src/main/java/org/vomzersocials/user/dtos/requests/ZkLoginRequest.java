package org.vomzersocials.user.dtos.requests;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.vomzersocials.user.enums.LoginMethod;


@Data
public class ZkLoginRequest {
//    @NotEmpty(message = "Username cannot be empty")
//    private String userName;

    @NotEmpty(message = "Public key cannot be empty")
    private String jwt;

}

