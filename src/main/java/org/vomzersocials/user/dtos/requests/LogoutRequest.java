package org.vomzersocials.user.dtos.requests;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LogoutRequest {
    public String userName;
    private String message;
}
