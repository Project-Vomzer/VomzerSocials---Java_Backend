package org.vomzersocials.user.dtos.requests;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LogoutRequest {
    private String username;
    private String refreshToken;
    private String message;
}
