package org.vomzersocials.data.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.vomzersocials.data.models.Post;
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
}
