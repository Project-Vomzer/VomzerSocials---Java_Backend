package org.vomzersocials.user.services.implementations;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.vomzersocials.user.data.models.User;
import org.vomzersocials.user.data.models.UserFollowing;
import org.vomzersocials.user.data.repositories.FollowRepository;
import org.vomzersocials.user.data.repositories.UserRepository;
import org.vomzersocials.user.dtos.requests.FollowUserRequest;
import org.vomzersocials.user.services.interfaces.FollowerService;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Slf4j
@Service
public class FollowerServiceImpl implements FollowerService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    public FollowerServiceImpl(FollowRepository followRepository, UserRepository userRepository) {
        this.followRepository = followRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Mono<Void> followUser(FollowUserRequest followUserRequest) {
        log.info("Attempting to follow user: followerId={}, followingId={}",
                followUserRequest.getFollowerId(), followUserRequest.getFollowingId());
        return validateFollowUserRequest(followUserRequest)
                .then(Mono.defer(() -> Mono.fromCallable(() -> {
                    log.debug("Fetching follower with ID: {}", followUserRequest.getFollowerId());
                    Optional<User> followerOpt = userRepository.findUserById(followUserRequest.getFollowerId());
                    return followerOpt.orElseThrow(() -> new IllegalArgumentException("Follower not found"));
                }).subscribeOn(Schedulers.boundedElastic())))
                .zipWith(Mono.defer(() -> Mono.fromCallable(() -> {
                    log.debug("Fetching following with ID: {}", followUserRequest.getFollowingId());
                    Optional<User> followingOpt = userRepository.findUserById(followUserRequest.getFollowingId());
                    return followingOpt.orElseThrow(() -> new IllegalArgumentException("User to follow not found"));
                }).subscribeOn(Schedulers.boundedElastic())))
                .flatMap(tuple -> {
                    User follower = tuple.getT1();
                    User following = tuple.getT2();
                    if (follower.getId().equals(following.getId())) {
                        return Mono.error(new IllegalArgumentException("You cannot follow yourself"));
                    }
                    return Mono.fromCallable(() -> followRepository.existsByFollowerIdAndFollowingId(follower.getId(), following.getId()))
                            .subscribeOn(Schedulers.boundedElastic())
                            .flatMap(alreadyFollowing -> {
                                if (alreadyFollowing) {
                                    return Mono.error(new IllegalStateException("You are already following this user"));
                                }
                                return setUserFollowingAnotherUser(follower, following)
                                        .then(updateUserFollowCountsAndSaveUsers(follower, following, true));
                            });
                })
                .onErrorMap(Throwable.class, e -> {
                    log.error("Error in followUser: followerId={}, followingId={}",
                            followUserRequest.getFollowerId(), followUserRequest.getFollowingId(), e);
                    return new RuntimeException("Failed to follow user: " + e.getMessage());
                })
                .then();
    }

    private Mono<Void> validateFollowUserRequest(FollowUserRequest request) {
        if (request.getFollowerId() == null || request.getFollowerId().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Follower ID cannot be null or empty"));
        }
        if (request.getFollowingId() == null || request.getFollowingId().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Following ID cannot be null or empty"));
        }
        return Mono.empty();
    }

    @Override
    public Mono<Void> unfollowUser(FollowUserRequest followUserRequest) {
        String followerId = followUserRequest.getFollowerId();
        String followingId = followUserRequest.getFollowingId();
        log.info("Attempting to unfollow user: followerId={}, followingId={}", followerId, followingId);
        return validateFollowUserRequest(followUserRequest)
                .then(Mono.fromCallable(() -> followRepository.findByFollowerIdAndFollowingId(followerId, followingId)
                                .orElseThrow(() -> new IllegalArgumentException("You are not following this user")))
                        .subscribeOn(Schedulers.boundedElastic()))
                .flatMap(userFollowing -> Mono.fromCallable(() -> {
                    followRepository.delete(userFollowing);
                    return userFollowing;
                }).subscribeOn(Schedulers.boundedElastic()))
                .then(Mono.defer(() -> Mono.fromCallable(() -> {
                    Optional<User> followerOpt = userRepository.findUserById(followerId);
                    return followerOpt.orElseThrow(() -> new IllegalArgumentException("Follower not found"));
                }).subscribeOn(Schedulers.boundedElastic())))
                .zipWith(Mono.defer(() -> Mono.fromCallable(() -> {
                    Optional<User> followingOpt = userRepository.findUserById(followingId);
                    return followingOpt.orElseThrow(() -> new IllegalArgumentException("User to unfollow not found"));
                }).subscribeOn(Schedulers.boundedElastic())))
                .flatMap(tuple -> {
                    User follower = tuple.getT1();
                    User following = tuple.getT2();
                    return updateUserFollowCountsAndSaveUsers(follower, following, false);
                })
                .onErrorMap(Throwable.class, e -> {
                    log.error("Error in unfollowUser: followerId={}, followingId={}", followerId, followingId, e);
                    return new RuntimeException("Failed to unfollow user: " + e.getMessage());
                })
                .then();
    }

    @Override
    public Mono<Boolean> isFollowing(FollowUserRequest followUserRequest) {
        return Mono.fromCallable(() -> followRepository.findByFollowerIdAndFollowingId(
                                followUserRequest.getFollowerId(), followUserRequest.getFollowingId())
                        .map(UserFollowing::getIsFollowing)
                        .orElse(false))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Void> toggleFollow(FollowUserRequest request) {
        String followerId = request.getFollowerId();
        String followingId = request.getFollowingId();
        log.info("Attempting to toggle follow: followerId={}, followingId={}", followerId, followingId);
        return validateFollowUserRequest(request)
                .then(Mono.just(followerId.equals(followingId))
                        .flatMap(isSelfFollow -> {
                            if (isSelfFollow) {
                                return Mono.error(new IllegalArgumentException("You cannot follow yourself"));
                            }
                            return Mono.fromCallable(() -> followRepository.findByFollowerIdAndFollowingId(followerId, followingId))
                                    .subscribeOn(Schedulers.boundedElastic());
                        }))
                .flatMap(existingFollow -> {
                    if (existingFollow.isPresent()) {
                        // Unfollow
                        return Mono.fromCallable(() -> {
                                    followRepository.delete(existingFollow.get());
                                    return existingFollow.get();
                                }).subscribeOn(Schedulers.boundedElastic())
                                .then(Mono.defer(() -> Mono.fromCallable(() -> {
                                    Optional<User> followerOpt = userRepository.findUserById(followerId);
                                    return followerOpt.orElseThrow(() -> new IllegalArgumentException("Follower not found"));
                                }).subscribeOn(Schedulers.boundedElastic())))
                                .zipWith(Mono.defer(() -> Mono.fromCallable(() -> {
                                    Optional<User> followingOpt = userRepository.findUserById(followingId);
                                    return followingOpt.orElseThrow(() -> new IllegalArgumentException("User to unfollow not found"));
                                }).subscribeOn(Schedulers.boundedElastic())))
                                .flatMap(tuple -> {
                                    User follower = tuple.getT1();
                                    User following = tuple.getT2();
                                    return updateUserFollowCountsAndSaveUsers(follower, following, false);
                                });
                    } else {
                        // Follow
                        return Mono.defer(() -> Mono.fromCallable(() -> {
                                    Optional<User> followerOpt = userRepository.findUserById(followerId);
                                    return followerOpt.orElseThrow(() -> new IllegalArgumentException("Follower not found"));
                                }).subscribeOn(Schedulers.boundedElastic()))
                                .zipWith(Mono.defer(() -> Mono.fromCallable(() -> {
                                    Optional<User> followingOpt = userRepository.findUserById(followingId);
                                    return followingOpt.orElseThrow(() -> new IllegalArgumentException("User to follow not found"));
                                }).subscribeOn(Schedulers.boundedElastic())))
                                .flatMap(tuple -> {
                                    User follower = tuple.getT1();
                                    User following = tuple.getT2();
                                    return setUserFollowingAnotherUser(follower, following)
                                            .then(updateUserFollowCountsAndSaveUsers(follower, following, true));
                                });
                    }
                })
                .onErrorMap(Throwable.class, e -> {
                    log.error("Error in toggleFollow: followerId={}, followingId={}", followerId, followingId, e);
                    return new RuntimeException("Failed to toggle follow: " + e.getMessage());
                })
                .then();
    }

    private Mono<Void> updateUserFollowCountsAndSaveUsers(User follower, User following, boolean isIncrement) {
        return Mono.fromCallable(() -> {
            if (isIncrement) {
                follower.setFollowingCount(follower.getFollowingCount() + 1);
                following.setFollowerCount(following.getFollowerCount() + 1);
            } else {
                follower.setFollowingCount(Math.max(0, follower.getFollowingCount() - 1));
                following.setFollowerCount(Math.max(0, following.getFollowerCount() - 1));
            }
            userRepository.saveAll(List.of(follower, following));
            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    private Mono<UserFollowing> setUserFollowingAnotherUser(User follower, User following) {
        return Mono.fromCallable(() -> {
            UserFollowing userFollowing = new UserFollowing();
            userFollowing.setFollowedAt(LocalDateTime.now());
            userFollowing.setIsFollowing(true);
            userFollowing.setFollower(follower);
            userFollowing.setFollowing(following);
            return followRepository.save(userFollowing);
        }).subscribeOn(Schedulers.boundedElastic());
    }
}