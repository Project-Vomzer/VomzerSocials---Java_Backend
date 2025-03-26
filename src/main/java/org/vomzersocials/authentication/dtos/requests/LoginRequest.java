package org.vomzersocials.authentication.dtos.requests;

import lombok.*;
import org.vomzersocials.utils.Role;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LoginRequest {
    private String message;
    private String jwtToken;
    private Role role;
    private String username;
    private String password;
}
