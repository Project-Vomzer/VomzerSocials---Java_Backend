package org.vomzersocials.user.dtos.requests;

import lombok.Getter;
import lombok.Setter;
import org.vomzersocials.user.data.models.User;

@Setter
@Getter
public class CreatePostRequest {
    private String title;
    private User author;
    private String content;
}
