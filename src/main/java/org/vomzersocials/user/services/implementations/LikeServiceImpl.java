package org.vomzersocials.user.services.implementations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.vomzersocials.user.data.models.Like;
import org.vomzersocials.user.data.models.Post;
import org.vomzersocials.user.data.models.User;
import org.vomzersocials.user.data.repositories.LikeRepository;
import org.vomzersocials.user.data.repositories.PostRepository;
import org.vomzersocials.user.data.repositories.UserRepository;
import org.vomzersocials.user.dtos.requests.LikeRequest;
import org.vomzersocials.user.dtos.responses.LikeResponse;
import org.vomzersocials.user.services.interfaces.LikeService;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    @Override
    public Mono<LikeResponse> likeOrUnLike(LikeRequest likeRequest) {
        return Mono.just(likeRequest)
                .filter(req -> req != null)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("LikeRequest cannot be null")))
                .filter(req -> req.getUserId() != null && !req.getUserId().isEmpty())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("User ID cannot be null or empty")))
                .filter(req -> req.getPostId() != null && !req.getPostId().isEmpty())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Post ID cannot be null or empty")))
                .doOnNext(req -> log.info("Processing like/unlike: userId={}, postId={}",
                        req.getUserId(), req.getPostId()))
                .flatMap(req -> userRepository.findById(req.getUserId())
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("User not found")))
                        .zipWith(postRepository.findById(req.getPostId())
                                .switchIfEmpty(Mono.error(new IllegalArgumentException("Post not found")))))
                .flatMap(tuple -> {
                    User user = tuple.getT1();
                    Post post = tuple.getT2();
                    return likeRepository.findByUserAndPost(user, post)
                            .flatMap(existingLike -> {
                                LikeResponse likeResponse = new LikeResponse();
                                if (existingLike != null) {
                                    // Unlike
                                    existingLike.setIsLiked(false);
                                    return likeRepository.save(existingLike)
                                            .map(savedLike -> {
                                                likeResponse.setMessage("Unliked");
                                                likeResponse.setLiked(false);
                                                return likeResponse;
                                            });
                                } else {
                                    // Like
                                    Like like = new Like();
                                    like.setUser(user);
                                    like.setPost(post);
                                    like.setIsLiked(true);
                                    return likeRepository.save(like)
                                            .map(savedLike -> {
                                                likeResponse.setMessage("Liked");
                                                likeResponse.setLiked(true);
                                                return likeResponse;
                                            });
                                }
                            });
                })
                .doOnSuccess(response -> log.info("Like operation completed: userId={}, postId={}, liked={}",
                        likeRequest.getUserId(), likeRequest.getPostId(), response.isLiked()))
                .onErrorMap(Throwable.class, e -> {
                    log.error("Error in likeOrUnLike: userId={}, postId={}",
                            likeRequest.getUserId(), likeRequest.getPostId(), e);
                    return new RuntimeException("Failed to like/unlike post: " + e.getMessage());
                });
    }


}
