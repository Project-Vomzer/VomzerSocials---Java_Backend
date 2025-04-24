package org.vomzersocials.user.dtos.responses;


import lombok.*;
import org.vomzersocials.user.data.models.Role;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LoginResponse {
    private String message;
    private String jwtToken;
    private Role role;
    private String userName;


    public LoginResponse(String userName, String loggedInSuccessfully) {
        this.userName = userName;
        this.message = loggedInSuccessfully;
    }
}


