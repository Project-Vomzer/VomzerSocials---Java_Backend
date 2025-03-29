//package org.vomzersocials.zkLogin.security;
//
//import lombok.AllArgsConstructor;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Service;
//import org.vomzersocials.user.data.repositories.TokenRepository;
//import org.vomzersocials.user.services.interfaces.TokenService;
//import org.vomzersocials.zkLogin.models.Token;
//
//import java.security.SecureRandom;
//import java.time.LocalDateTime;
//
//
//@Service
//@AllArgsConstructor
//public class TokenServiceImpl implements TokenService {
//
//    private final TokenRepository tokenRepository;
//    @Override
//    public String createToken(String email) {
//        String token = generateToken();
//        Token userToken = new Token();
//        userToken.setToken(token);
//        userToken.setUserName(userToken.getUserName());
//        userToken.setTimeCreated(LocalDateTime.now());
//        Token savedToken = tokenRepository.save(userToken);
//        return savedToken.getToken();
//    }
//
//    private String generateToken() {
//        StringBuilder token = new StringBuilder();
//
//        for (int count = 0; count < 5; count++) {
//            SecureRandom secureRandom = new SecureRandom();
//            int numbers = secureRandom.nextInt(1, 9);
//            token.append(numbers);
//        }
//        return String.valueOf(token);
//    }
//
//    @Override
//    public Token findByUserName(String username) {
//        return tokenRepository.findTokenByUserName(username)
//                .orElseThrow(() -> new UsernameNotFoundException("Provide valid mail"));
//    }
//
//    @Override
//    public void deleteToken(String id) {
//        tokenRepository.deleteById(id);
//}
//
//}
