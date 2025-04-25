package org.vomzersocials.zkLogin.services;

import org.springframework.stereotype.Service;
import org.vomzersocials.user.data.models.User;
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

    /**
     * Registration flow: verify the zk proof, then store the resulting Sui address
     * alongside the newly created user in the DB.
     *
     * @param zkProof  the proof blob from the client
     * @param userName the username under which to register
     * @param publicKey the public key for zk-proof verification
     * @return the verified Sui address, or null if proof invalid
     */
    public String registerViaZkProof(String zkProof, String userName, String publicKey) {
        // 1️⃣ Verify proof with the SuiZkLoginClient (passing publicKey for verification)
        String suiAddress = suiZkLoginClient.verifyProof(zkProof, publicKey);
        if (suiAddress == null) {
            return null;   // invalid proof
        }

        // 2️⃣ Persist the mapping userName → Sui address
        User user = userRepository.findUserByUserName(userName)
                .orElseThrow(() -> new IllegalArgumentException("User not found for zk-registration"));
        user.setSuiAddress(suiAddress);
        userRepository.save(user);

        return suiAddress;
    }

    /**
     * Login flow: verify the zk proof and return the Sui address if valid.
     * The AuthenticationService.loginUser(...) can then trust this as proof of identity.
     *
     * @param zkProof the proof blob from the client
     * @param publicKey the public key for zk-proof verification
     * @return the verified Sui address, or null if invalid
     */
    public String loginViaZkProof(String zkProof, String publicKey) {
        return suiZkLoginClient.verifyProof(zkProof, publicKey);
    }
}
