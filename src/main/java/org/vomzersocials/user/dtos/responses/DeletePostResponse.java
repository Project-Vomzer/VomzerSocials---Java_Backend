package org.vomzersocials.user.dtos.responses;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DeletePostResponse {
    private String postId;
    private String message;
}
