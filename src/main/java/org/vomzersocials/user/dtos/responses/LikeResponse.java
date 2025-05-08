package org.vomzersocials.user.dtos.responses;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LikeResponse {
    private String message;
    private boolean isLiked;
}
