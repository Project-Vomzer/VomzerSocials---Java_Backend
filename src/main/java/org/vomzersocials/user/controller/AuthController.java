package org.vomzersocials.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.vomzersocials.user.data.models.User;
import org.vomzersocials.user.data.repositories.UserRepository;
import org.vomzersocials.user.dtos.requests.*;
import org.vomzersocials.user.dtos.responses.*;
import org.vomzersocials.user.exceptions.UsernameNotFoundException;
import org.vomzersocials.user.services.interfaces.TokenService;
import org.vomzersocials.user.services.interfaces.UserService;
import org.vomzersocials.user.springSecurity.JwtUtil;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.time.Duration;

@CrossOrigin(origins = "${cors.allowed-origins:http://localhost:5173,http://localhost:5174 }")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @Value("${cookie.secure:true}")
    private boolean cookieSecure;



    @PostMapping("/login/standard")
    public Mono<ResponseEntity<LoginResponse>> loginStandard(@Valid @RequestBody StandardLoginRequest request) {
        return userService.loginUserViaStandard(request)
                .map(response -> {
                    log.info("Standard login successful for username: {}", request.getUserName());
                    ResponseCookie cookie = ResponseCookie.from("refreshToken", response.getRefreshToken())
                            .httpOnly(true)
                            .secure(cookieSecure)
                            .path("/")
                            .maxAge(Duration.ofDays(7))
                            .sameSite("Strict")
                            .build();
                    return ResponseEntity.ok()
                            .header(HttpHeaders.SET_COOKIE, cookie.toString())
                            .body(response);
                });
    }

    @PostMapping("/login/zk")
    public Mono<ResponseEntity<LoginResponse>> loginZk(@Valid @RequestBody ZkLoginRequest request) {
        return userService.loginUserViaZk(request)
                .map(response -> {
//                    log.info("zkLogin successful for publicKey: {}", request.getPublicKey());
                    ResponseCookie cookie = ResponseCookie.from("refreshToken", response.getRefreshToken())
                            .httpOnly(true)
                            .secure(cookieSecure)
                            .path("/")
                            .maxAge(Duration.ofDays(7))
                            .sameSite("Strict")
                            .build();
                    return ResponseEntity.ok()
                            .header(HttpHeaders.SET_COOKIE, cookie.toString())
                            .body(response);
                });
    }

    @PostMapping("/register/admin")
    public Mono<ResponseEntity<RegisterUserResponse>> registerAdmin(
            @Valid @RequestBody StandardRegisterRequest request) {
        return userService.registerAdmin(request)
                .map(response -> {
                    log.info("Admin registration successful for username: {}", request.getUserName());
                    return ResponseEntity.created(URI.create("/api/users/" + response.getUsername()))
                            .body(response);
                })
                .onErrorResume(e -> {
                    HttpStatus status = (e instanceof IllegalArgumentException) ? HttpStatus.BAD_REQUEST : HttpStatus.INTERNAL_SERVER_ERROR;
                    return Mono.just(ResponseEntity.status(status).body(
                            RegisterUserResponse.builder()
                                    .message(e.getMessage())
                                    .build()
                    ));
                });
    }


    @PostMapping("/register/standard")
    public Mono<ResponseEntity<RegisterUserResponse>> registerStandard(@Valid @RequestBody StandardRegisterRequest request) {
        return userService.registerNewUserViaStandardRegistration(request)
                .map(response -> {
                    log.info("Standard registration successful for username: {}", request.getUserName());
                    return ResponseEntity.created(URI.create("/api/users/" + response.getUsername()))
                            .body(response);
                });
    }

    @PostMapping("/register/zk")
    public Mono<ResponseEntity<RegisterUserResponse>> registerZk(@Valid @RequestBody ZkRegisterRequest request) {
        return userService.registerNewUserViaZk(request)
                .map(response -> {
                    log.info("zk registration successful for username: {}", request.getUserName());
                    return ResponseEntity.created(URI.create("/api/users/" + response.getUsername()))
                            .body(response);
                });
    }

    @PostMapping("/logout")
    public Mono<ResponseEntity<LogoutUserResponse>> logout(@Valid @RequestBody LogoutRequest request) {
        return ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> securityContext.getAuthentication().getName())
                .flatMap(username -> {
                    if (!username.equals(request.getUsername())) {
                        return Mono.error(new SecurityException("Unauthorized: Token does not match user"));
                    }
                    return userService.logoutUser(request);
                })
                .map(response -> {
                    log.info("Logout successful for username: {}", request.getUsername());
                    ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                            .httpOnly(true)
                            .secure(cookieSecure)
                            .path("/")
                            .maxAge(0)
                            .sameSite("Strict")
                            .build();
                    return ResponseEntity.ok()
                            .header(HttpHeaders.SET_COOKIE, cookie.toString())
                            .body(response);
                });
    }

    @PostMapping("/refresh")
    public Mono<ResponseEntity<TokenPair>> refreshToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Mono.error(new SecurityException("Missing or invalid Authorization header"));
        }
        String refreshToken = authHeader.replaceFirst("^Bearer\\s+", "");
        return userService.refreshTokens(refreshToken)
                .map(response -> {
                    log.info("Token refresh successful via header");
                    ResponseCookie cookie = ResponseCookie.from("refreshToken", response.getRefreshToken())
                            .httpOnly(true)
                            .secure(cookieSecure)
                            .path("/")
                            .maxAge(Duration.ofDays(7))
                            .sameSite("Strict")
                            .build();
                    return ResponseEntity.ok()
                            .header(HttpHeaders.SET_COOKIE, cookie.toString())
                            .body(response);
                });
    }

    @PostMapping("/refresh-token")
    public Mono<ResponseEntity<TokenPair>> refreshTokenCookie(
            @CookieValue(name = "refreshToken", required = false) String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return Mono.error(new SecurityException("Missing or invalid refresh token"));
        }
        return userService.refreshTokens(refreshToken)
                .map(response -> {
                    log.info("Token refresh successful via cookie for user: {}", jwtUtil.extractUsername(refreshToken));
                    ResponseCookie cookie = ResponseCookie.from("refreshToken", response.getRefreshToken())
                            .httpOnly(true)
                            .secure(cookieSecure)
                            .path("/")
                            .maxAge(Duration.ofDays(7))
                            .sameSite("Strict")
                            .build();
                    return ResponseEntity.ok()
                            .header(HttpHeaders.SET_COOKIE, cookie.toString())
                            .body(response);
                })
                .onErrorMap(IllegalArgumentException.class, e -> new IllegalArgumentException("Token refresh failed: " + e.getMessage()))
                .onErrorMap(SecurityException.class, e -> new SecurityException("Token refresh failed: " + e.getMessage()));
    }

    @PostMapping("/password-reset/request")
    public Mono<ResponseEntity<String>> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        return Mono.fromCallable(() -> userRepository.findUserByUserName(request.getUserName())
                        .orElseThrow(() -> new IllegalArgumentException("User not found")))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(user -> tokenService.createToken(request.getUserName())
                        .map(token -> {
                            log.info("Password reset token created for user: {}", request.getUserName());
                            // TODO: Send token to user (e.g., via notification system, not email)
                            return ResponseEntity.ok("Password reset token generated. Check your notifications.");
                        }))
                .onErrorMap(IllegalArgumentException.class, e -> new IllegalArgumentException("Password reset request failed: " + e.getMessage()));
    }

    @PostMapping("/password-reset/verify")
    public Mono<ResponseEntity<String>> verifyPasswordReset(@Valid @RequestBody PasswordResetVerifyRequest request) {
        return tokenService.findByUserName(request.getUserName())
                .flatMap(token -> {
                    if (!token.getToken().equals(request.getToken())) {
                        return Mono.error(new IllegalArgumentException("Invalid reset token"));
                    }
                    return Mono.fromCallable(() -> {
                                User user = userRepository.findUserByUserName(request.getUserName())
                                        .orElseThrow(() -> new IllegalArgumentException("User not found"));
                                user.setPassword(passwordEncoder.encode(request.getNewPassword()));
                                userRepository.save(user);
                                return user;
                            })
                            .subscribeOn(Schedulers.boundedElastic())
                            .flatMap(savedUser ->
                                    tokenService.deleteToken(token.getId())
                                            .thenReturn(ResponseEntity.ok("Password reset successfully"))
                            );
                })
                .onErrorMap(IllegalArgumentException.class, e -> new IllegalArgumentException("Password reset failed: " + e.getMessage()))
                .onErrorMap(UsernameNotFoundException.class, e -> new IllegalArgumentException("Password reset failed: " + e.getMessage()));
    }


}

