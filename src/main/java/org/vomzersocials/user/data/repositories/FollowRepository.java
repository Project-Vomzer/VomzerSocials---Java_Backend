package org.vomzersocials.user.data.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.vomzersocials.user.data.models.FollowId;
import org.vomzersocials.user.data.models.Follower;

import java.util.List;

public interface FollowRepository extends JpaRepository<Follower, FollowId> {
    boolean existsByIdFollowerIdAndIdFollowingId(String followerId, String followingId);
    void deleteByIdFollowerIdAndIdFollowingId(String followerId, String followingId);
    List<Follower> findAllByIdFollowerId(String followerId);
}
