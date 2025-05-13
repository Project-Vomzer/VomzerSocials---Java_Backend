package org.vomzersocials.user.dtos.responses;

import lombok.*;
import org.vomzersocials.user.enums.Role;

@Data
@NoArgsConstructor
@ToString
public class LoginResponse {
    private String username;
    private String message;
    private String accessToken;
    private String refreshToken;
//    private Role role;
    private String loginMethod;


    public LoginResponse(String username, String message, String accessToken, String refreshToken, String loginMethod) {
        this.username = username;
        this.message = message;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
//        this.role = role;
        this.loginMethod = loginMethod;
    }

    public LoginResponse(String userName, String loggedInSuccessfully, String accessToken) {
        this.username = userName;
        this.message = loggedInSuccessfully;
        this.accessToken = accessToken;
//        this.role = role;
    }


}


