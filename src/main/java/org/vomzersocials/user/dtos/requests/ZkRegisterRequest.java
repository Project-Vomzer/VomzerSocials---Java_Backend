package org.vomzersocials.user.dtos.requests;


import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
public class ZkRegisterRequest {
    @NotEmpty(message = "Username cannot be empty")
    private String userName;

    @NotEmpty(message = "JWT cannot be empty")
    private String jwt;

}