package org.vomzersocials.data.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.vomzersocials.data.models.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

}
