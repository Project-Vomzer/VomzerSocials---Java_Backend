package org.vomzersocials.user.dtos.responses;

import lombok.*;
import org.vomzersocials.user.enums.Role;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
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
