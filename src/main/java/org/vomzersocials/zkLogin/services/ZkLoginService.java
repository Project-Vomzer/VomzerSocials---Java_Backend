package org.vomzersocials.zkLogin.services;

import org.springframework.stereotype.Service;
import org.vomzersocials.user.data.models.User;
import org.vomzersocials.zkLogin.security.VerifiedAddressResult;
import org.vomzersocials.user.data.repositories.UserRepository;
import org.vomzersocials.zkLogin.security.SuiZkLoginClient;

@Service
public class ZkLoginService {

    private final SuiZkLoginClient suiZkLoginClient;
    private final UserRepository userRepository;

    public ZkLoginService(SuiZkLoginClient suiZkLoginClient, UserRepository userRepository) {
        this.suiZkLoginClient = suiZkLoginClient;
        this.userRepository = userRepository;
    }
    public String registerViaZkProof(String zkProof, String userName, String publicKey) {
        String suiAddress = String.valueOf(suiZkLoginClient.verifyProof(zkProof, publicKey));
        if (suiAddress == null) {
            return null;
        }

        User user = userRepository.findUserByUserName(userName)
                .orElseThrow(() -> new IllegalArgumentException("User not found for zk-registration"));

        user.setSuiAddress(suiAddress);
        userRepository.save(user);

        return suiAddress;
    }

    public VerifiedAddressResult loginViaZkProof(String zkProof, String publicKey) {
        return suiZkLoginClient.verifyProof(zkProof, publicKey);
    }
}
