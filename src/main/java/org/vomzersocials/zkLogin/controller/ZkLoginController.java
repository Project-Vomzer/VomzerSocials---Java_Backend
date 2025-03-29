//package org.vomzersocials.zkLogin.controller;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.vomzersocials.zkLogin.dtos.ZkLoginRequest;
//import org.vomzersocials.zkLogin.security.ZkLoginService;
//
//public class ZkLoginController {
//
//    @Autowired
//    private ZkLoginService zkLoginService;
//
//    public ResponseEntity<String> loginWithZk(@RequestBody ZkLoginRequest zkLoginRequest){
//        boolean isValid = zkLoginService.verifyZkProof(zkLoginRequest.getZkProof());
//
//        if (isValid){
//            return ResponseEntity.ok("Zk proof verified successfully");
//        } else {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Zk proof verification failed");
//        }
//    }
//}
