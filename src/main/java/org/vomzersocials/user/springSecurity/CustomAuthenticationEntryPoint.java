//package org.vomzersocials.user.springSecurity;
//
//import jakarta.servlet.ServletException;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.web.AuthenticationEntryPoint;
//import org.springframework.stereotype.Component;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//
//@Component
//public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint, org.vomzersocials.user.springSecurity.AuthenticationEntryPoint {
//
//    @Override
//    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
//        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: Invalid or expired JWT token");
//    }
//
//    @Override
//    public void commence(jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
//
//    }
//}
package org.vomzersocials.user.springSecurity;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class CustomAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

    @Override
    public Mono<Void> commence(ServerWebExchange exchange,
                               AuthenticationException ex) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        // Optionally write a JSON error body:
        // DataBuffer buffer = exchange.getResponse()
        //    .bufferFactory()
        //    .wrap("{\"error\":\"Unauthorized: invalid or expired token\"}".getBytes());
        // exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        // return exchange.getResponse().writeWith(Mono.just(buffer));
        return exchange.getResponse().setComplete();
    }
}
