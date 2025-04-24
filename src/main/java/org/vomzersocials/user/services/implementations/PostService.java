package org.vomzersocials.user.services.implementations;

import org.springframework.stereotype.Service;
import org.vomzersocials.user.data.repositories.PostRepository;
import org.vomzersocials.user.utils.Post;

import java.util.UUID;

@Service
public class PostService implements org.vomzersocials.user.services.interfaces.PostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

//    public void deletePostWithMedia(UUID postId){
//        Post post = postRepository.findById(postId)
//                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
//    }
}
