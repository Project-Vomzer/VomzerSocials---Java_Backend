package org.vomzersocials.user.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.vomzersocials.user.data.models.Role;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterUserResponse {
    private String userName;
    private Boolean isLoggedIn;
    private Role role;
    private String message;

    public RegisterUserResponse(String userName, boolean b, String s) {
        this.userName = userName;
        this.isLoggedIn = b;
        this.message = s;
    }
}
