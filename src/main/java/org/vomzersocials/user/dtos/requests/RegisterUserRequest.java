package org.vomzersocials.user.dtos.requests;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import org.vomzersocials.utils.Role;

@Setter
@Getter
public class RegisterUserRequest {
    private String userName;
    private String password;
    private Boolean isLoggedIn;
    private Role role;


}
