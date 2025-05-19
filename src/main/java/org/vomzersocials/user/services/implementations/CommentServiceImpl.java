package org.vomzersocials.user.services.implementations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.vomzersocials.user.data.models.Comment;
import org.vomzersocials.user.data.models.Post;
import org.vomzersocials.user.data.models.User;
import org.vomzersocials.user.data.repositories.CommentRepository;
import org.vomzersocials.user.data.repositories.PostRepository;
import org.vomzersocials.user.data.repositories.UserRepository;
import org.vomzersocials.user.dtos.requests.CreateCommentRequest;
import org.vomzersocials.user.dtos.responses.CreateCommentResponse;
import org.vomzersocials.user.exceptions.PostNotFoundException;
import org.vomzersocials.user.exceptions.UserDoesNotExistException;
import org.vomzersocials.user.services.interfaces.CommentService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    @Override
    public CreateCommentResponse createComment(CreateCommentRequest createCommentRequest) {
        // Validate input
        if (createCommentRequest == null) {
            throw new IllegalArgumentException("CreateCommentRequest cannot be null");
        }
        if (createCommentRequest.getPostId() == null || createCommentRequest.getPostId().isEmpty()) {
            throw new IllegalArgumentException("Post ID cannot be null or empty");
        }
        if (createCommentRequest.getUserId() == null || createCommentRequest.getUserId().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        if (createCommentRequest.getContent() == null || createCommentRequest.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Comment content cannot be null or empty");
        }

        log.info("Creating comment: postId={}, userId={}",
                createCommentRequest.getPostId(), createCommentRequest.getUserId());

        // Retrieve post
        Post post = postRepository.findById(createCommentRequest.getPostId())
                .orElseThrow(() -> new PostNotFoundException("Post not found"));

        // Retrieve user (fix bug)
        User user = userRepository.findUserById(createCommentRequest.getUserId())
                .orElseThrow(() -> new UserDoesNotExistException("User not found"));

        // Create comment
        Comment comment = new Comment();
        comment.setUser(user);
        comment.setPost(post);
        comment.setContent(createCommentRequest.getContent());

        // Handle parent comment
        if (createCommentRequest.getParentCommentId() != null) {
            try {
                UUID parentCommentId = UUID.fromString(createCommentRequest.getParentCommentId());
                Comment parentComment = commentRepository.findById(parentCommentId)
                        .orElseThrow(() -> new IllegalArgumentException("Parent comment not found"));
                comment.setParentComment(parentComment);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid parent comment ID format", e);
            }
        }

        // Save comment
        Comment savedComment = commentRepository.save(comment);

        // Build response
        CreateCommentResponse createCommentResponse = new CreateCommentResponse();
        createCommentResponse.setCreatedAt(LocalDateTime.now());
        createCommentResponse.setPostId(post.getId());
        createCommentResponse.setUserId(user.getId());
        createCommentResponse.setContent(savedComment.getContent());
        createCommentResponse.setId(savedComment.getId());

        log.info("Comment created: commentId={}", savedComment.getId());
        return createCommentResponse;
    }

    @Override
    public List<CreateCommentResponse> getCommentsByPost(String postId) {
        List<Comment> topLevelComments = commentRepository.findByPostIdAndParentCommentIsNull(postId);

        if (topLevelComments.isEmpty()) throw new PostNotFoundException("Post not found or no comments available");
        return topLevelComments.stream()
                .map(this::mapToCommentResponseWithReplies)
                .toList();
    }

    @Override
    public List<Comment> getCommentsForPost(String postId) {
        if (!postRepository.existsById(postId)) {
            throw new PostNotFoundException("Post not found");
        }
        return commentRepository.findByPostId(postId);
    }

    @Override
    public List<Comment> getReplies(String commentId) {
        Comment parentComment = commentRepository.findById(UUID.fromString(commentId))
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

        return commentRepository.findByParentComment(parentComment);
    }

    @Override
    public void deleteComment(String commentId) {
        Comment comment = commentRepository.findById(UUID.fromString(commentId))
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

        deleteTrailingCommentsToavoidHavingOrphanedComments(comment);
    }

    private void deleteTrailingCommentsToavoidHavingOrphanedComments(Comment comment) {
        List<Comment> replies = commentRepository.findByParentComment(comment);
        for (Comment reply : replies) {
            deleteTrailingCommentsToavoidHavingOrphanedComments(reply);
        }
        commentRepository.delete(comment);
    }



    private CreateCommentResponse mapToCommentResponseWithReplies(Comment comment) {
        CreateCommentResponse response = new CreateCommentResponse();
        response.setId(comment.getId());
        response.setContent(comment.getContent());
        response.setUserId(comment.getUser().getId());
        response.setPostId(comment.getPost().getId());
        response.setCreatedAt(comment.getCreatedAt());

        List<CreateCommentResponse> replies = commentRepository.findByParentComment(comment).stream()
                .map(this::mapToCommentResponseWithReplies)
                .toList();

        response.setReplies(replies);
        return response;
    }




}


