package org.vomzersocials.user.data.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.vomzersocials.user.data.models.User;
import java.util.Optional;

public interface UserRepository extends R2dbcRepository<User, String> {
    Optional<User> findUserByUserName(String userName);
    Optional<User> findUserBySuiAddress(String suiAddress);
    Optional<User> findByPublicKey(String publicKey);
    Optional<User> findByJwtSubjectHash(String jwtSubjectHash);
    Optional<User> findUserById(String userId);
    void saveAllUser(Iterable<User> user);
}