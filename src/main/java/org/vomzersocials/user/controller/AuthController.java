package org.vomzersocials.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.vomzersocials.user.dtos.requests.RegisterUserRequest;
import org.vomzersocials.user.dtos.responses.RegisterUserResponse;
import org.vomzersocials.user.services.interfaces.UserService;
//import org.vomzersocials.zkLogin.dtos.AuthResponse;
//import org.vomzersocials.zkLogin.dtos.ZkLoginRequest;
//import org.vomzersocials.zkLogin.services.ZkLoginService;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private UserService userService;

//    private final ZkLoginService zkLoginService;

//    public AuthController(ZkLoginService zkLoginService) {
//        this.zkLoginService = zkLoginService;
//    }

//    @PostMapping("/zklogin")
//    public ResponseEntity<?> authenticateWithZkLogin(@RequestBody ZkLoginRequest request, HttpSession session) {
//        String suiAddress = zkLoginService.verifyZkLogin(request.getZkProof());
//
//        if (suiAddress == null) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid zkLogin proof");
//        }
//
//        session.setAttribute("user", suiAddress);
//
//        return ResponseEntity.ok(new AuthResponse("Authenticated successfully " + suiAddress));
//    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterUserRequest registerUserRequest) {
        try{
            RegisterUserResponse response = userService.registerNewUser(registerUserRequest);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
