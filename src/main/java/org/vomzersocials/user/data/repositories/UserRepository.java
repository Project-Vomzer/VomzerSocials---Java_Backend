package org.vomzersocials.user.data.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.vomzersocials.user.data.models.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findUserByUserName(String userName);
    Optional<User> findUserBySuiAddress(String suiAddress);
    Optional<User> findByPublicKey(String publicKey);
    Optional<User> findByJwtSubjectHash(String jwtSubjectHash);
}