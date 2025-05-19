package org.vomzersocials.user.services.interfaces;

import org.vomzersocials.user.dtos.requests.LikeRequest;
import org.vomzersocials.user.dtos.responses.LikeResponse;
import reactor.core.publisher.Mono;

public interface LikeService {
    Mono<LikeResponse> likeOrUnLike(LikeRequest likeRequest);
}
