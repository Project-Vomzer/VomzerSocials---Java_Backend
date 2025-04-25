package org.vomzersocials.user.dtos.responses;


import lombok.*;
import org.vomzersocials.user.enums.Role;

@Setter
@Getter
@NoArgsConstructor
@ToString
public class LoginResponse {
    private String username;
    private String message;
    private String accessToken;
    private String refreshToken;

    public LoginResponse(String username, String message, String accessToken, String refreshToken) {
        this.username = username;
        this.message = message;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}


