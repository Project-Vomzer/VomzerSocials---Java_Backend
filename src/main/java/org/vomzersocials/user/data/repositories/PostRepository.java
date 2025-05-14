package org.vomzersocials.user.data.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.vomzersocials.user.data.models.Post;
import org.vomzersocials.user.data.models.User;

import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, String> {
    boolean existsByAuthorAndRepostedPost(Optional<User> foundUser, Post foundPost);
}
