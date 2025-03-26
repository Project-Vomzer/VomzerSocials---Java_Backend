package org.vomzersocials.authentication.dtos.responses;


import lombok.*;
import org.vomzersocials.utils.Role;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LoginResponse {
    private String message;
    private String jwtToken;
    private Role role;
    private String firstName;
    private String lastName;


}


