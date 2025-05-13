package org.vomzersocials.user.dtos.responses;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.vomzersocials.user.data.models.User;
import java.time.LocalDateTime;

@Setter
@Getter
@Builder
public class CreatePostResponse {
    private String id;
    private String authorId;
    private String content;
    private String message;
    private LocalDateTime timestamp;
    private String errorMessage;
}
