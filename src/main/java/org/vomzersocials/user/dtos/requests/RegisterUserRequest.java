package org.vomzersocials.user.dtos.requests;

import lombok.Getter;
import lombok.Setter;
import org.vomzersocials.user.enums.Role;


@Setter
@Getter
public class RegisterUserRequest {
    private String userName;
    private String password;
    private Role role;
    private String zkProof;
}
