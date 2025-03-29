//package org.vomzersocials.zkLogin.security;
//
//import org.springframework.stereotype.Service;
//import org.vomzersocials.user.data.repositories.UserRepository;
//
//@Service
//public class ZkLoginService {
//
//    private final SuiZkLoginClient suiZkLoginClient;
//    private final UserRepository userRepository;
//
//    public ZkLoginService(SuiZkLoginClient suiZkLoginClient, UserRepository userRepository) {
//        this.suiZkLoginClient = suiZkLoginClient;
//        this.userRepository = userRepository;
//    }
//
//    public String verifyZkLogin(String zkProof) {
//        String suiAddress = suiZkLoginClient.verifyProof(zkProof);
//
//        if (suiAddress != null) {
//            // Store or update user info in the database
//            userRepository.updateUserSuiAddress(suiAddress);
//        }
//
//        return suiAddress;
//    }
//}
//
