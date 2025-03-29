package org.vomzersocials.user.dtos.responses;

import lombok.Getter;
import lombok.Setter;
import org.vomzersocials.user.data.models.User;
import java.time.LocalDateTime;

@Setter
@Getter
public class CreatePostResponse {
    private String title;
    private User author;
    private String content;
    private String message;
    private LocalDateTime timestamp;

}
