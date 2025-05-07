//package org.vomzersocials.zkLogin.services;
//
//import org.junit.jupiter.api.Test;
//import org.mockito.Mock;
//import org.vomzersocials.user.data.models.User;
//import org.vomzersocials.user.data.repositories.UserRepository;
//import org.vomzersocials.zkLogin.security.SuiZkLoginClient;
//
//import java.util.Optional;
//
//import static reactor.core.publisher.Mono.when;
//
//public class ZkLoginServiceTest {
//
//    @Mock
//    private SuiZkLoginClient suiZkLoginClient;
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private ZkLoginService zkLoginService;
//
//    @Test
//    public void registerWithZkProof_registerSucceeds_suiAddressIsGeneratedAndStored_test(){
//        User user = new User();
//        when(userRepository.findUserByUserName("Bob")).thenReturn(Optional.of(user));
//        when(suiZkLoginClient.verifyProof("zkProof", "publicKey")).thenReturn("0xaddr")
//    }
//
//}