package org.vomzersocials.user.services.interfaces;

public interface FollowerService {
    void followUser(String followerId, String followingId);
    void unfollowUser(String followerId, String followingId);
    boolean isFollowing(String followerId, String followingId);
}
