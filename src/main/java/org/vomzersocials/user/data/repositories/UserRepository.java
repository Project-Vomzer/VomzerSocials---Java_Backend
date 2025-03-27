package org.vomzersocials.user.data.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.vomzersocials.user.data.models.User;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    User findUserByUserName(String loginRequest);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.suiAddress = :suiAddress WHERE u.id = :id")
    void updateUserSuiAddress(@Param("suiAddress") String suiAddress);
    Optional<User> findByPublicKey(String publicKey);

}
