package org.vomzersocials.user.data.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.vomzersocials.user.data.models.UserFollowing;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FollowRepository extends JpaRepository<UserFollowing, String> {
    boolean existsByFollowerIdAndFollowingId(String id, String id1);
    Optional<UserFollowing> findByFollowerIdAndFollowingId(String followerId, String followingId);
    UserFollowing save(UserFollowing userFollowing);
    void delete(UserFollowing userFollowing);
}
