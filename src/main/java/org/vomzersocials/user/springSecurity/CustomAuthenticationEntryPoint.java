////package org.vomzersocials.user.springSecurity;
////
////import com.fasterxml.jackson.databind.ObjectMapper;
////import org.slf4j.Logger;
////import org.slf4j.LoggerFactory;
////import org.springframework.http.MediaType;
////import org.springframework.security.core.AuthenticationException;
////import org.springframework.stereotype.Component;
////import org.springframework.web.server.ServerWebExchange;
////import reactor.core.publisher.Mono;
////
////import java.util.HashMap;
////import java.util.Map;
////
////@Component
////public class CustomAuthenticationEntryPoint implements org.springframework.security.web.server.ServerAuthenticationEntryPoint {
////    private static final Logger log = LoggerFactory.getLogger(CustomAuthenticationEntryPoint.class);
////    private final ObjectMapper objectMapper = new ObjectMapper();
////
////    @Override
////    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException authException) {
////        log.warn("Unauthorized access to {}: {}", exchange.getRequest().getURI(), authException.getMessage());
////
////        exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
//////        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
////
////        Map<String, Object> error = new HashMap<>();
////        error.put("status", org.springframework.http.HttpStatus.UNAUTHORIZED.value());
////        error.put("error", "Unauthorized");
////        error.put("message", authException.getMessage());
////        error.put("path", exchange.getRequest().getURI().getPath());
////
////        try {
////            byte[] data = objectMapper.writeValueAsBytes(error);
////            return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(data)));
////        } catch (Exception e) {
////            log.error("Failed to write error response", e);
////            return Mono.error(e);
////        }
////    }
////}
//package org.vomzersocials.user.springSecurity;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.stereotype.Component;
//import org.springframework.web.server.ServerWebExchange;
//import reactor.core.publisher.Mono;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@Component
//public class CustomAuthenticationEntryPoint implements org.springframework.security.web.server.ServerAuthenticationEntryPoint {
//    private static final Logger log = LoggerFactory.getLogger(CustomAuthenticationEntryPoint.class);
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    @Override
//    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException authException) {
//        log.warn("Unauthorized access to {}: {}", exchange.getRequest().getURI(), authException.getMessage());
//
//        exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
////        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
//
//        Map<String, Object> error = new HashMap<>();
//        error.put("status", org.springframework.http.HttpStatus.UNAUTHORIZED.value());
//        error.put("error", "Unauthorized");
//        error.put("message", authException.getMessage());
//        error.put("path", exchange.getRequest().getURI().getPath());
//
//        try {
//            byte[] data = objectMapper.writeValueAsBytes(error);
//            return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(data)));
//        } catch (Exception e) {
//            log.error("Failed to write error response", e);
//            return Mono.error(e);
//        }
//    }
//}
package org.vomzersocials.user.springSecurity;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationEntryPoint.class);
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException authException) {
        String safeMessage = sanitizeMessage(authException.getMessage());
        logger.warn("Authentication failed for {}: {}", exchange.getRequest().getURI().getPath(), safeMessage);

        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        try {
            String json = mapper.writeValueAsString(new ErrorResponse("Authentication failed", safeMessage));
            return exchange.getResponse().writeWith(Mono.just(
                    exchange.getResponse().bufferFactory().wrap(json.getBytes())
            ));
        } catch (Exception e) {
            logger.warn("Failed to serialize error response: {}", e.getMessage());
            String fallbackJson = "{\"error\":\"Authentication failed\",\"details\":\"Internal error\"}";
            return exchange.getResponse().writeWith(Mono.just(
                    exchange.getResponse().bufferFactory().wrap(fallbackJson.getBytes())
            ));
        }
    }

    private String sanitizeMessage(String message) {
        if (message == null) {
            return "Unknown authentication error";
        }
        return message.replaceAll("[\\p{Cntrl}\\p{Cc}\\p{Cf}\\p{Co}\\p{Cn}]", "")
                .trim()
                .substring(0, Math.min(message.length(), 200));
    }

    private record ErrorResponse(String error, String details) {
    }
}