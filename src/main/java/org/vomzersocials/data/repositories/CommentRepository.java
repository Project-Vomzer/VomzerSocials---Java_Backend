package org.vomzersocials.data.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.vomzersocials.data.models.Comment;
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
}
