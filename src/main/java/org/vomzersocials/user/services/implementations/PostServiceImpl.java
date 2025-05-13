//package org.vomzersocials.user.services.implementations;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.vomzersocials.user.data.models.Media;
//import org.vomzersocials.user.data.models.Post;
//import org.vomzersocials.user.data.models.User;
//import org.vomzersocials.user.data.repositories.PostRepository;
//import org.vomzersocials.user.data.repositories.UserRepository;
//import org.vomzersocials.user.dtos.requests.CreatePostRequest;
//import org.vomzersocials.user.dtos.requests.DeletePostRequest;
//import org.vomzersocials.user.dtos.requests.EditPostRequest;
//import org.vomzersocials.user.dtos.requests.RepostRequest;
//import org.vomzersocials.user.dtos.responses.CreatePostResponse;
//import org.vomzersocials.user.dtos.responses.DeletePostResponse;
//import org.vomzersocials.user.dtos.responses.EditPostResponse;
//import org.vomzersocials.user.dtos.responses.RepostResponse;
//import org.vomzersocials.user.services.interfaces.PostService;
//import org.vomzersocials.user.services.interfaces.UserService;
//import reactor.core.publisher.Mono;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
//@Slf4j
//@Service
//public class PostServiceImpl implements PostService {
//
//    private final PostRepository postRepository;
//    private final UserRepository userRepository;
//    private final MediaServiceImpl mediaService;
//
//    public PostServiceImpl(PostRepository postRepository, UserRepository userRepository, MediaServiceImpl mediaService) {
//        this.postRepository = postRepository;
//        this.userRepository = userRepository;
//        this.mediaService = mediaService;
//    }
//
//    @Override
//    public Mono<CreatePostResponse> createPost(CreatePostRequest createPostRequest) {
//        return Mono.fromCallable(() -> {
//            if (createPostRequest.getAuthor() == null ||
//                    createPostRequest.getContent() == null || createPostRequest.getContent().trim().isEmpty()) {
//                throw new IllegalArgumentException("Author and content are required");
//            }
//
//            User user = userRepository.findUserByUserName(createPostRequest.getAuthor().getUserName())
//                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
//
//            if (!user.getIsLoggedIn()) throw new IllegalArgumentException("User is not logged in");
//
//            Post post = new Post();
//            post.setAuthor(user);
//            post.setContent(createPostRequest.getContent());
//            post.setCreatedAt(LocalDateTime.now());
//            post.setUpdatedAt(LocalDateTime.now());
//
//            List<Media> mediaList = mediaService.getMediaByIds(createPostRequest.getMediaIds());
//            post.setMediaList(mediaList);
//
//            Post savedPost = postRepository.save(post);
//            log.info("saved post: {}", savedPost.getId());
//
//            CreatePostResponse createPostResponse = new CreatePostResponse();
//            createPostResponse.setAuthor(savedPost.getAuthor());
//            createPostResponse.setTimestamp(savedPost.getCreatedAt());
//            createPostResponse.setId(savedPost.getId());
//            createPostResponse.setContent(savedPost.getContent());
//            return createPostResponse;
//        });
//    }
//
//    @Override
//    @Transactional
//    public Mono<DeletePostResponse> deletePost(DeletePostRequest deletePostRequest) {
//        return Mono.fromCallable(() -> {
//            User foundUser = userRepository.findUserById(deletePostRequest.getUserId());
//            if (!foundUser.getIsLoggedIn()) throw new IllegalArgumentException("User is not logged in");
//
//            log.info("foundUser: {}", foundUser);
//            log.info("deletePostRequest: {}", deletePostRequest.getPostId());
//
//            Optional<Post> optionalPost = postRepository.findById(deletePostRequest.getPostId());
//            if (optionalPost.isEmpty()) throw new IllegalArgumentException("Post not found");
//
//            Post foundPost = optionalPost.get();
//            if (!foundPost.getAuthor().getId().equals(foundUser.getId())) {
//                throw new SecurityException("User is not authorized to delete this post");
//            }
//            for (Media media : foundPost.getMediaList()) {
//                try {
//                    mediaService.deleteMediaById(media.getId());
//                } catch (Exception exception) {
//                    log.error("Error deleting media: {}", media.getId(), exception);
//                }
//
//            }
//            postRepository.delete(foundPost);
//
//            DeletePostResponse deletePostResponse = new DeletePostResponse();
//            deletePostResponse.setMessage("Post deleted successfully");
//            deletePostResponse.setPostId(foundPost.getId());
//            return deletePostResponse;
//        });
//    }
//
//    @Override
//    public Mono<EditPostResponse> editPost(EditPostRequest editPostRequest) {
//        return Mono.fromCallable(() -> {
//            Post foundPost = postRepository.findById(editPostRequest.getPostId())
//                    .orElseThrow(() -> new IllegalArgumentException("Post not found"));
//
//            log.info("Found post id: {}", foundPost.getId());
//            if (!editPostRequest.getUserId().equals(foundPost.getAuthor().getId())) {
//                throw new SecurityException("User not authorized to edit this post");
//            }
//            foundPost.setContent(editPostRequest.getContent());
//            foundPost.setUpdatedAt(LocalDateTime.now());
//            postRepository.save(foundPost);
//
//            EditPostResponse editPostResponse = new EditPostResponse();
//            editPostResponse.setMessage("Post edited successfully");
//            editPostResponse.setId(foundPost.getId());
//            editPostResponse.setContent(foundPost.getContent());
//            editPostResponse.setTimestamp(foundPost.getUpdatedAt());
//            return editPostResponse;
//        });
//    }
//
//
//    public void deletePostWithMedia(UUID postId){
//        Post post = postRepository.findById(String.valueOf(postId))
//                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
//        for (Media media: post.getMediaList()) {
//            mediaService.deleteMediaById(media.getId());
//        }
//        postRepository.delete(post);
//    }
//
//    @Override
//    public Mono<RepostResponse> repost(RepostRequest repostRequest, String userId) {
//        return Mono.fromCallable(() -> {
//            Optional<User> foundUser = userRepository.findById(userId);
//            if (foundUser.isEmpty()) throw new IllegalArgumentException("User not found");
//            if (!Boolean.TRUE.equals(foundUser.get().getIsLoggedIn()))
//                throw new IllegalArgumentException("User is not logged in");
//
//            Post foundPost = postRepository.findById(repostRequest.getPostId())
//                    .orElseThrow(() -> new IllegalArgumentException("Post not found"));
//
//            if (foundPost.getAuthor().getId().equals(foundUser.get().getId()))
//                throw new IllegalArgumentException("You cannot repost your own post");
//
//            boolean existingRepost = postRepository.existsByAuthorAndRepostedPost(foundUser, foundPost);
//            if (existingRepost) throw new IllegalArgumentException("You already reposted this post");
//
//            LocalDateTime dateTimeNow = LocalDateTime.now();
//
//            Post repost = new Post();
//            repost.setAuthor(foundUser.get());
//            repost.setRepostedPost(foundPost);
//            repost.setCreatedAt(dateTimeNow);
//
//            postRepository.save(repost);
//            foundPost.setRepostCount(foundPost.getRepostCount() + 1);
//            postRepository.save(foundPost);
//
//            RepostResponse repostResponse = new RepostResponse();
//            repostResponse.setFoundUserId(foundUser.get().getId());
//            repostResponse.setRepostedPostId(foundPost.getId());
//            repostResponse.setCreatedAt(dateTimeNow);
//
//            return repostResponse;
//        });
//    }
//
//}

package org.vomzersocials.user.services.implementations;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.vomzersocials.user.data.models.Media;
import org.vomzersocials.user.data.models.Post;
import org.vomzersocials.user.data.models.User;
import org.vomzersocials.user.data.repositories.PostRepository;
import org.vomzersocials.user.data.repositories.UserRepository;
import org.vomzersocials.user.dtos.requests.*;
import org.vomzersocials.user.dtos.responses.*;
import org.vomzersocials.user.exceptions.OwnershipException;
import org.vomzersocials.user.exceptions.PostNotFoundException;
import org.vomzersocials.user.services.interfaces.PostService;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final MediaServiceImpl mediaService;

    public PostServiceImpl(PostRepository postRepository,
                           UserRepository userRepository,
                           MediaServiceImpl mediaService) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.mediaService = mediaService;
    }

    @Override
    public Mono<CreatePostResponse> createPost(CreatePostRequest request, String userId) {
        return Mono.fromCallable(() -> {
            if (request.getContent() == null || request.getContent().trim().isEmpty())
                throw new IllegalArgumentException("Post content cannot be empty");
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            if (!user.getIsLoggedIn())
                throw new SecurityException("User must be logged in to create posts");
            Post post = new Post();
            post.setAuthor(user);
            post.setContent(request.getContent());
            post.setCreatedAt(LocalDateTime.now());
            post.setUpdatedAt(LocalDateTime.now());

            if (request.getMediaIds() != null && !request.getMediaIds().isEmpty()) {
                List<Media> mediaList = mediaService.getMediaByIds(request.getMediaIds());
                post.setMediaList(mediaList);
            }
            Post savedPost = postRepository.save(post);
            return CreatePostResponse.builder()
                    .id(savedPost.getId())
                    .content(savedPost.getContent())
                    .authorId(userId)
                    .timestamp(savedPost.getCreatedAt())
                    .build();
        });
    }

    @Override
    @Transactional
    public Mono<DeletePostResponse> deletePost(DeletePostRequest request, String userId) {
        return Mono.fromCallable(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            if (!user.getIsLoggedIn())
                throw new SecurityException("User authentication required");
            Post post = postRepository.findById(request.getPostId())
                    .orElseThrow(() -> new PostNotFoundException(request.getPostId()));

            if (!post.getAuthor().getId().equals(userId))
                throw new OwnershipException(userId, request.getPostId());
            post.getMediaList().forEach(media ->
                    mediaService.deleteMediaById(media.getId()));
            postRepository.delete(post);

            return DeletePostResponse.builder()
                    .postId(post.getId())
                    .message("Post deleted successfully")
                    .build();
        });
    }

    @Override
    public Mono<EditPostResponse> editPost(EditPostRequest request, String userId) {
        return Mono.fromCallable(() -> {
            Post post = postRepository.findById(request.getPostId())
                    .orElseThrow(() -> new PostNotFoundException(request.getPostId()));

            if (!post.getAuthor().getId().equals(userId))
                throw new OwnershipException(userId, request.getPostId());

            post.setContent(request.getContent());
            post.setUpdatedAt(LocalDateTime.now());
            Post updatedPost = postRepository.save(post);

            return EditPostResponse.builder()
                    .id(updatedPost.getId())
                    .content(updatedPost.getContent())
                    .timestamp(updatedPost.getUpdatedAt())
                    .isEdited(true)
                    .author(updatedPost.getAuthor())
                    .build();
        });
    }

    @Override
    public Mono<RepostResponse> repost(RepostRequest request, String userId) {
        return Mono.fromCallable(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            if (!user.getIsLoggedIn())
                throw new SecurityException("User must be logged in to repost");
            Post originalPost = postRepository.findById(request.getPostId())
                    .orElseThrow(() -> new PostNotFoundException(request.getPostId()));

            if (originalPost.getAuthor().getId().equals(userId))
                throw new IllegalArgumentException("Cannot repost your own content");

            if (postRepository.existsByAuthorAndRepostedPost(Optional.of(user), originalPost))
                throw new IllegalArgumentException("Already reposted this content");

            Post repost = new Post();
            repost.setAuthor(user);
            repost.setRepostedPost(originalPost);
            repost.setCreatedAt(LocalDateTime.now());
            postRepository.save(repost);

            originalPost.setRepostCount(originalPost.getRepostCount() + 1);
            postRepository.save(originalPost);
            return RepostResponse.builder()
                    .repostedPostId(originalPost.getId())
                    .foundUserId(userId)
                    .createdAt(LocalDateTime.now())
                    .build();
        });
    }

    public void deletePostWithMedia(UUID postId) {
        Post post = postRepository.findById(postId.toString())
                .orElseThrow(() -> new PostNotFoundException(postId.toString()));
        post.getMediaList().forEach(media ->
                mediaService.deleteMediaById(media.getId()));
        postRepository.delete(post);
    }
}
