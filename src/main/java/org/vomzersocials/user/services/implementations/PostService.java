package org.vomzersocials.user.services.implementations;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vomzersocials.user.data.models.Post;
import org.vomzersocials.user.data.models.User;
import org.vomzersocials.user.data.repositories.PostRepository;
import org.vomzersocials.user.data.repositories.UserRepository;
import org.vomzersocials.user.dtos.requests.CreatePostRequest;
import org.vomzersocials.user.dtos.requests.DeletePostRequest;
import org.vomzersocials.user.dtos.requests.EditPostRequest;
import org.vomzersocials.user.dtos.responses.CreatePostResponse;
import org.vomzersocials.user.dtos.responses.DeletePostResponse;
import org.vomzersocials.user.dtos.responses.EditPostResponse;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
public class PostService implements org.vomzersocials.user.services.interfaces.PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PostService(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    @Override
    public CreatePostResponse createPost(CreatePostRequest createPostRequest) {
        if (createPostRequest.getAuthor() == null ||
                createPostRequest.getContent() == null || createPostRequest.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Author and content are required");
        }

        User user = userRepository.findUserByUserName(createPostRequest.getAuthor().getUserName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!user.getIsLoggedIn()) throw new IllegalArgumentException("User is not logged in");

        Post post = new Post();
        post.setAuthor(user);
        post.setContent(createPostRequest.getContent());
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());

        Post savedPost = postRepository.save(post);
        log.info("saved post: {}", savedPost.getId());

        CreatePostResponse createPostResponse = new CreatePostResponse();
        createPostResponse.setAuthor(savedPost.getAuthor());
        createPostResponse.setTimestamp(savedPost.getCreatedAt());
        createPostResponse.setId(savedPost.getId());
        createPostResponse.setContent(savedPost.getContent());
        log.info("post id 2: {}", savedPost.getId());
        return createPostResponse;
    }

    @Override
    public DeletePostResponse deletePost(DeletePostRequest deletePostRequest) {
        User foundUser = userRepository.findUserById(deletePostRequest.getUserId());
        if (!foundUser.getIsLoggedIn()) throw new IllegalArgumentException("User is not logged in");

        log.info("foundUser: " + foundUser);
        log.info("deletePostRequest: " + deletePostRequest.getPostId());

        Optional<Post> optionalPost = postRepository.findById(deletePostRequest.getPostId());
        if (optionalPost.isEmpty()) throw new IllegalArgumentException("Post not found");

        Post foundPost = optionalPost.get();
        if (!foundPost.getAuthor().getId().equals(foundUser.getId())) {
            throw new SecurityException("User is not authorized to delete this post");
        }
        foundPost.setUpdatedAt(LocalDateTime.now());
        postRepository.delete(foundPost);

        DeletePostResponse deletePostResponse = new DeletePostResponse();
        deletePostResponse.setMessage("Post deleted successfully");
        deletePostResponse.setPostId(foundPost.getId());
        return deletePostResponse;
    }

    @Override
    public EditPostResponse editPost(EditPostRequest editPostRequest) {
        Post foundPost = postRepository.findById(editPostRequest.getPostId())
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        log.info("Found post id: {}", foundPost.getId());
        if (!editPostRequest.getUserId().equals(foundPost.getAuthor().getId())) {
            throw new SecurityException("User not authorized to edit this post");
        }
        foundPost.setContent(editPostRequest.getContent());
        foundPost.setUpdatedAt(LocalDateTime.now());
        postRepository.save(foundPost);

        EditPostResponse editPostResponse = new EditPostResponse();
        editPostResponse.setMessage("Post edited successfully");
        editPostResponse.setId(foundPost.getId());
        editPostResponse.setContent(foundPost.getContent());
        editPostResponse.setTimestamp(foundPost.getUpdatedAt());
        return editPostResponse;
    }


    public void deletePostWithMedia(UUID postId){
        Post post = postRepository.findById(String.valueOf(postId))
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
    }
}
