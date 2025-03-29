package org.vomzersocials.user.dtos.requests;

import lombok.*;
import org.vomzersocials.user.data.models.Role;

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
