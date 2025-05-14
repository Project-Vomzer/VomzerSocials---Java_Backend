package org.vomzersocials.user.dtos.responses;

import lombok.*;
import org.vomzersocials.user.enums.Role;

@Data
@NoArgsConstructor
@ToString
@AllArgsConstructor

public class LoginResponse {
    private String username;
    private String message;
    private String accessToken;
    private String refreshToken;
    private Role role;
    private String loginMethod;

}


