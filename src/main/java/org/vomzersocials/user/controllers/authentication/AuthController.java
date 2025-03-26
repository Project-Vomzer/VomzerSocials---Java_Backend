package org.vomzersocials.user.controllers.authentication;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.vomzersocials.user.data.models.AuthResponse;
import org.vomzersocials.user.data.models.ZkLoginRequest;
import org.vomzersocials.user.services.implementations.ZkLoginService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final ZkLoginService zkLoginService;

    public AuthController(ZkLoginService zkLoginService) {
        this.zkLoginService = zkLoginService;
    }

    @PostMapping("/zklogin")
    public ResponseEntity<?> authenticateWithZkLogin(@RequestBody ZkLoginRequest request, HttpSession session) {
        String suiAddress = zkLoginService.verifyZkLogin(request.getZkProof());

        if (suiAddress == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid zkLogin proof");
        }

        // Store user session (if needed)
        session.setAttribute("user", suiAddress);

        return ResponseEntity.ok(new AuthResponse("Authenticated successfully " + suiAddress));
    }
}
