package org.vomzersocials.user.dtos.requests;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class EditPostRequest {
    private String postId;
    private String userId;
    private String content;
}
