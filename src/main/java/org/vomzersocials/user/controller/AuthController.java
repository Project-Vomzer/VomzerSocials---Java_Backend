package org.vomzersocials.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.vomzersocials.user.dtos.responses.JwtResponse;
import org.vomzersocials.zkLogin.dtos.ZkLoginRequest;
import org.vomzersocials.zkLogin.services.ZkLoginAuthService;

import javax.naming.AuthenticationException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final ZkLoginAuthService zkLoginAuthService;

    public AuthController(ZkLoginAuthService zkLoginAuthService) {
        this.zkLoginAuthService = zkLoginAuthService;
    }

    // Endpoint for user login
    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@RequestBody ZkLoginRequest request) {
        try {
            // Authenticate and get JWT token
            String jwtToken = zkLoginAuthService.authenticateUser(request);
            return ResponseEntity.ok(new JwtResponse(jwtToken)); // Return JWT token in response
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Request error: " + e.getMessage());
        }
    }
}
