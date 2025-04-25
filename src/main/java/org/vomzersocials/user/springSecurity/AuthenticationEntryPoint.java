package org.vomzersocials.user.springSecurity;

import org.springframework.security.core.AuthenticationException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface AuthenticationEntryPoint {
    void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException;
}
