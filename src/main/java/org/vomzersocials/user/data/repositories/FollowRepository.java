package org.vomzersocials.user.data.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.vomzersocials.user.data.models.FollowId;
import org.vomzersocials.user.data.models.Follow;

import java.util.List;
@Repository
public interface FollowRepository extends JpaRepository<Follow, FollowId> {
//    boolean existsByIdFollowerIdAndIdFollowingId(String followerId, String followingId);
//    void deleteByIdFollowerIdAndIdFollowingId(String followerId, String followingId);
//    List<Follow> findAllByIdFollowerId(String followerId);
    List<Follow> findByFollowerId(String followId);
    List<Follow> findByFollowingId(String followingId);
    boolean existsById(FollowId id);
    void deleteById(FollowId id);
}
