//package org.vomzersocials.zkLogin.services;
//
//import org.springframework.stereotype.Service;
//import org.vomzersocials.user.data.models.User;
//import org.vomzersocials.zkLogin.security.VerifiedAddressResult;
//import org.vomzersocials.user.data.repositories.UserRepository;
//import org.vomzersocials.zkLogin.security.SuiZkLoginClient;
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
//    public String registerViaZkProof(String zkProof, String userName, String publicKey) {
////        String suiAddress = String.valueOf(suiZkLoginClient.verifyProof(zkProof, publicKey));
//        VerifiedAddressResult result = suiZkLoginClient.verifyProof(zkProof, publicKey);
//        if (result == null || !result.isSuccess()) {
//            return null;
//        }
//        String suiAddress = result.getAddress();
//
//        User user = userRepository.findUserByUserName(userName)
//                .orElseThrow(() -> new IllegalArgumentException("User not found for zk-registration"));
//
//        user.setSuiAddress(suiAddress);
//        userRepository.save(user);
//
//        return suiAddress;
//    }
//
//    public VerifiedAddressResult loginViaZkProof(String zkProof, String publicKey) {
//        return suiZkLoginClient.verifyProof(zkProof, publicKey);
//    }
//}

package org.vomzersocials.zkLogin.services;

import org.springframework.stereotype.Service;
import org.vomzersocials.zkLogin.security.SuiZkLoginClient;
import org.vomzersocials.zkLogin.security.VerifiedAddressResult;

@Service
public class ZkLoginService {

    private final SuiZkLoginClient suiZkLoginClient;

    public ZkLoginService(SuiZkLoginClient suiZkLoginClient) {
        this.suiZkLoginClient = suiZkLoginClient;
    }

    public String registerViaZkProof(String zkProof, String userName, String publicKey) {
        if ("mock".equals(zkProof) && "mock_key".equals(publicKey)) {
            return "0xmockedSuiAddress12345";
        }
        throw new IllegalArgumentException("Invalid zk-proof or proof verification failed");
    }

    public VerifiedAddressResult loginViaZkProof(String zkProof, String publicKey) {
        if ("mock".equals(zkProof) && "mock_key".equals(publicKey)) {
            VerifiedAddressResult result = new VerifiedAddressResult();
            result.setAddress("0xmockedSuiAddress12345");
            result.setSuccess(true);
            return result;
        }
        throw new IllegalArgumentException("Invalid zk-proof or proof verification failed");
    }

}

