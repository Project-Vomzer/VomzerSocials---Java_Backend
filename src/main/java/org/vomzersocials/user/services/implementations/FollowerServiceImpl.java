package org.vomzersocials.user.services.implementations;

import org.springframework.stereotype.Service;
import org.vomzersocials.user.data.models.FollowId;
import org.vomzersocials.user.data.models.Follower;
import org.vomzersocials.user.data.repositories.FollowRepository;
import org.vomzersocials.user.services.interfaces.FollowerService;

@Service
public class FollowerServiceImpl implements FollowerService {

    private final FollowRepository followRepository;

    public FollowerServiceImpl(FollowRepository followRepository) {
        this.followRepository = followRepository;
    }

    @Override
    public void followUser(String followerId, String followingId) {
        FollowId followId = new FollowId();
        followId.setFollowerId(followerId);
        followId.setFollowingId(followingId);

        if (followRepository.existsById(followId)) throw new IllegalArgumentException("Already followed");
        Follower follower = new Follower();
        follower.setId(followId);
        follower.setIsFollowing(true);
        followRepository.save(follower);
    }

    @Override
    public void unfollowUser(String followerId, String followingId) {
        FollowId followId = new FollowId();
        followId.setFollowerId(followerId);
        followId.setFollowingId(followingId);

        if (!followRepository.existsById(followId)) throw new IllegalArgumentException("You are not following this user");
        followRepository.deleteById(followId);
    }

    @Override
    public boolean isFollowing(String followerId, String followingId) {
        return followRepository.existsByIdFollowerIdAndIdFollowingId(followerId, followingId);
    }
}
