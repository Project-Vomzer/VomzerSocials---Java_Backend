package org.vomzersocials.user.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.Map;

import org.vomzersocials.user.dtos.requests.*;
import org.vomzersocials.user.dtos.responses.*;
import org.vomzersocials.user.services.interfaces.AuthenticationService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {


    private final AuthenticationService auth;

    public AuthController(AuthenticationService auth) {
        this.auth = auth;
    }


//    @PostMapping("/register")
//    public Mono<ResponseEntity<Object>> register(@RequestBody RegisterUserRequest req) {
//        return Mono.defer(() -> auth.registerNewUser(req))
//                .map(dto -> ResponseEntity
//                        .created(URI.create("/api/users/" + dto.getUserName()))
//                        .body((Object) dto))
//                .onErrorResume(IllegalArgumentException.class, illegalArgumentException -> {
//                    Map<String, String> err = Map.of("error", illegalArgumentException.getMessage());
//                    return Mono.just(ResponseEntity
//                            .badRequest()
//                            .body((Object) err));
//                })
//                .onErrorResume(ex -> {
//                    log.error("Registration failure", ex);
//                    Map<String, String> err = Map.of("error", "Internal server error");
//                    return Mono.just(ResponseEntity
//                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
//                            .body((Object) err));
//                });
//    }
    @PostMapping("/register")
    public Mono<ResponseEntity<Object>> register(@RequestBody RegisterUserRequest req) {
        return Mono.defer(() -> auth.registerNewUser(req))
                .map(dto -> ResponseEntity.created(URI.create("/api/users/" + dto.getUserName()))
                        .body((Object) dto))
                .onErrorResume(IllegalArgumentException.class, ex -> {
                    log.warn("Registration error: {}", ex.getMessage());
                    Map<String, String> err = Map.of("error", ex.getMessage());
                    return Mono.just(ResponseEntity.badRequest().body((Object) err));
                })
                .onErrorResume(ex -> {
                    log.error("Registration failure", ex);
                    Map<String, String> err = Map.of("error", "Internal server error");
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body((Object) err));
                });
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<Object>> login(@RequestBody LoginRequest loginRequest) {
        log.info("Login attempt with method: {}", loginRequest.getLoginMethod()); // Log the method

        return auth.loginUser(loginRequest)
                .map(resp -> {
                    log.info("Login success response: {}", resp); // Log the full response

                    ResponseCookie cookie = ResponseCookie.from("refreshToken", resp.getRefreshToken())
                            .httpOnly(true).secure(true)
                            .path("/").maxAge(Duration.ofDays(7))
                            .sameSite("Strict").build();

                    return ResponseEntity.ok()
                            .header(HttpHeaders.SET_COOKIE, cookie.toString())
                            .body((Object) resp);
                })
                .onErrorResume(IllegalArgumentException.class, ex ->
                        Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(Map.of("error", ex.getMessage())))
                )
                .onErrorResume(ex -> {
                    log.error("Login failure", ex);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Map.of("error", "Internal server error")));
                });
    }


    @PostMapping("/logout")
    public Mono<ResponseEntity<Object>> logout(@Valid @RequestBody LogoutRequest logoutRequest) {
        return auth.logoutUser(logoutRequest)
                .map(resp -> ResponseEntity.ok((Object) resp))
                .onErrorResume(IllegalArgumentException.class, illegalArgumentException ->
                        Mono.<ResponseEntity<Object>>just(
                                ResponseEntity.badRequest()
                                        .body((Object) Map.of("error", illegalArgumentException.getMessage()))
                        ))
                .onErrorResume(logoutFailure -> {
                    log.error("Logout failure", logoutFailure);
                    return Mono.<ResponseEntity<Object>>just(
                            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                    .body((Object) Map.of("error", "Internal server error" + logoutFailure.getMessage()))
                    );
                });
    }

    @PostMapping("/refresh-token")
    public Mono<ResponseEntity<Object>> refreshToken(
            @CookieValue(name = "refreshToken", required = false) String refreshToken
    ) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }

        return auth.refreshTokens(refreshToken)
                .map(tokens -> {
                    ResponseCookie cookie = ResponseCookie.from("refreshToken", tokens.getRefreshToken())
                            .httpOnly(true).secure(true)
                            .path("/").maxAge(Duration.ofDays(7))
                            .sameSite("Strict").build();
                    return ResponseEntity.ok()
                            .header(HttpHeaders.SET_COOKIE, cookie.toString())
                            .body((Object) tokens);
                })
                .onErrorResume(IllegalArgumentException.class, ex ->
                        Mono.<ResponseEntity<Object>>just(
                                ResponseEntity.badRequest()
                                        .body((Object) Map.of("error", ex.getMessage()))
                        ))
                .onErrorResume(ex -> {
                    log.error("Refresh failure", ex);
                    return Mono.<ResponseEntity<Object>>just(
                            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                    .body((Object) Map.of("error", "Internal server error"))
                    );
                });
    }
}
