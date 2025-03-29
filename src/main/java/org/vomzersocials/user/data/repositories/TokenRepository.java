//package org.vomzersocials.user.data.repositories;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.vomzersocials.zkLogin.models.Token;
//
//import java.util.Optional;
//
//public interface TokenRepository extends JpaRepository<Token, String> {
//    Optional<Token> findTokenByUserName(String lowerCase);
//}