package org.vomzersocials.user.services.interfaces;

import org.springframework.stereotype.Service;
import org.vomzersocials.user.dtos.requests.CreatePostRequest;
import org.vomzersocials.user.dtos.requests.DeletePostRequest;
import org.vomzersocials.user.dtos.requests.EditPostRequest;
import org.vomzersocials.user.dtos.responses.CreatePostResponse;
import org.vomzersocials.user.dtos.responses.DeletePostResponse;
import org.vomzersocials.user.dtos.responses.EditPostResponse;

@Service
public interface PostService {
    CreatePostResponse createPost(CreatePostRequest createPostRequest);

    DeletePostResponse deletePost(DeletePostRequest deletePostRequest);

    EditPostResponse editPost(EditPostRequest editPostRequest);
}
