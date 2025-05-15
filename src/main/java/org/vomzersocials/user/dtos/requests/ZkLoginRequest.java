package org.vomzersocials.user.dtos.requests;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
public class ZkLoginRequest {
    @NotEmpty(message = "JWT cannot be empty")
    private String jwt;

}

