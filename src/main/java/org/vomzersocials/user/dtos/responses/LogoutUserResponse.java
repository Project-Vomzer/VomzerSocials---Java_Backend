package org.vomzersocials.user.dtos.responses;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LogoutUserResponse {
    private String userName;
    private String message;

    public LogoutUserResponse(String userName, String loggedOutSuccessfully) {
        this.userName = userName;
        this.message = loggedOutSuccessfully;
    }
}
