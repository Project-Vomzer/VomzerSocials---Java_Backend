package org.vomzersocials.user.dtos.requests;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LikeRequest {
    private String userId;
    private String postId;
}
