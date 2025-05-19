package org.vomzersocials.user.services.interfaces;

import org.vomzersocials.user.dtos.requests.FollowUserRequest;
import reactor.core.publisher.Mono;

public interface FollowerService {
    Mono<Void> followUser(FollowUserRequest request);
    Mono<Void> unfollowUser(FollowUserRequest request);
    Mono<Boolean> isFollowing(FollowUserRequest request);
    Mono<Void> toggleFollow(FollowUserRequest request);
}
