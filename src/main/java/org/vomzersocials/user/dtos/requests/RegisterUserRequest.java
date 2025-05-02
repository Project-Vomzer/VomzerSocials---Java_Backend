package org.vomzersocials.user.dtos.requests;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.vomzersocials.user.enums.Role;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegisterUserRequest {
    @JsonAlias({"username", "userName"})
    private String userName;
    private String password;
    private Role role;
    private String zkProof;
    private String publicKey;
}
