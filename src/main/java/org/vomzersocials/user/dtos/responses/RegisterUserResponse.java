package org.vomzersocials.user.dtos.responses;

import lombok.Getter;
import lombok.Setter;
import org.vomzersocials.user.utils.Role;

@Setter
@Getter
public class RegisterUserResponse {
    private String userName;
    private Boolean isLoggedIn;
    private Role role;
    private String message;
}
