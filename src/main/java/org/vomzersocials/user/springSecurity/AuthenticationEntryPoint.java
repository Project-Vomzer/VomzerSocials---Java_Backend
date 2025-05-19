package org.vomzersocials.user.springSecurity;

import org.springframework.security.core.AuthenticationException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.IOException;

public interface AuthenticationEntryPoint {
    Mono<Void> commence (ServerWebExchange exchange, AuthenticationException authException);
}
