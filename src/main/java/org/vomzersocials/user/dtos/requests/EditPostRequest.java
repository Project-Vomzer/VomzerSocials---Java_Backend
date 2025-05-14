package org.vomzersocials.user.dtos.requests;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EditPostRequest {
    private String postId;
    private String content;
}
