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
        // DataBuffer buffer = exchange.getResponse()
        //    .bufferFactory()
        //    .wrap("{\"error\":\"Unauthorized: invalid or expired token\"}".getBytes());
        // exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        // return exchange.getResponse().writeWith(Mono.just(buffer));
        return exchange.getResponse().setComplete();
    }
}
