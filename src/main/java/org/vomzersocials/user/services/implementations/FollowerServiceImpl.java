package org.vomzersocials.user.services.implementations;

import org.springframework.stereotype.Service;
import org.vomzersocials.user.data.models.FollowId;
import org.vomzersocials.user.data.models.Follow;
import org.vomzersocials.user.data.models.User;
import org.vomzersocials.user.data.repositories.FollowRepository;
import org.vomzersocials.user.data.repositories.UserRepository;
import org.vomzersocials.user.services.interfaces.FollowerService;

@Service
public class FollowerServiceImpl implements FollowerService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    public FollowerServiceImpl(FollowRepository followRepository, UserRepository userRepository) {
        this.followRepository = followRepository;
        this.userRepository = userRepository;
    }

    @Override
//    public void followUser(String followerId, String followingId) {
//        if (followerId.equals(followingId)) throw new IllegalArgumentException("You cannot follow yourself");
////        FollowId followId = new FollowId(followerId, followingId);
//
//        FollowId followId = new FollowId();
//        followId.setFollowerId(followerId);
//        followId.setFollowingId(followingId);
//
////        if (followRepository.existsById(followId)) throw new IllegalArgumentException("Already followed");
//        if (followRepository.existsById(followId)) return;
//
//        User follower = userRepository.findById(followerId)
//                .orElseThrow(() -> new IllegalArgumentException("Follower not found"));
//
//        User following = userRepository.findById(followingId)
//                .orElseThrow(() -> new IllegalArgumentException("User to follow not found"));
//
//        Follow follow = new Follow();
//        follow.setId(followId);
//        follow.setIsFollowing(true);
//        follow.setFollower(follower);
//        follow.setFollowing(following);
//        followRepository.save(follow);
//    }

    public void followUser(String followerId, String followingId) {
        Boolean userExists = checkIfUserToBeFollowedExists(followingId);
        if (userExists) {
            FollowId followId = new FollowId();
            followId.setFollowerId(followerId);
            followId.setFollowingId(followingId);

            User follower = userRepository.findById(followerId)
                    .orElseThrow(() -> new IllegalArgumentException("Follower not found"));

            User following = userRepository.findById(followingId)
                .orElseThrow(() -> new IllegalArgumentException("User to follow not found"));

            Follow follow = new Follow();
            follow.setId(followId);
            follow.setFollower(follower);
            follow.setFollowing(following);
            followRepository.save(follow);
        }
    }


    private Boolean checkIfUserToBeFollowedExists(String followingId) {
        for (User user : userRepository.findAll()) {
            if (user.getId().equals(followingId)) {
                return true;
            }
        }
        return false;
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
        return followRepository.existsById(new FollowId(followerId, followingId));
    }
}
