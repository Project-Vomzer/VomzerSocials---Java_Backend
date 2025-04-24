package org.vomzersocials.zkLogin.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.vomzersocials.user.data.repositories.PostRepository;
import org.vomzersocials.user.utils.Post;

import java.util.Optional;

@Component("postSecurity")
public class PostSecurity {

    @Autowired
    private PostRepository postRepository;

    public boolean isPostOwner(Long postId) {
        Optional<Post> post = postRepository.findById(String.valueOf(postId));
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        return post.map(p -> p.getAuthor().getUserName().equals(currentUsername)).orElse(false);
    }
}