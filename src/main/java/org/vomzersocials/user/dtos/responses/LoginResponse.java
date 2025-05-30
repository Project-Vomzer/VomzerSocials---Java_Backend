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
    private boolean loggedIn;


    public LoginResponse(String userName, String loggedInSuccessfully, String accessToken, String refreshToken, Role role, String loginMethod) {
        this.username = userName;
        this.message = loggedInSuccessfully;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.role = role;
        this.loginMethod = loginMethod;
        this.loggedIn = true;
    }
}


