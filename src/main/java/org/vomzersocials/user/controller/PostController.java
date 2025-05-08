package org.vomzersocials.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.vomzersocials.user.data.models.Comment;
import org.vomzersocials.user.dtos.requests.CreateCommentRequest;
import org.vomzersocials.user.dtos.requests.CreatePostRequest;
import org.vomzersocials.user.dtos.requests.DeletePostRequest;
import org.vomzersocials.user.dtos.requests.EditPostRequest;
import org.vomzersocials.user.dtos.responses.CreateCommentResponse;
import org.vomzersocials.user.dtos.responses.CreatePostResponse;
import org.vomzersocials.user.dtos.responses.DeletePostResponse;
import org.vomzersocials.user.dtos.responses.EditPostResponse;
import org.vomzersocials.user.services.interfaces.CommentService;
import org.vomzersocials.user.services.interfaces.FollowerService;
import org.vomzersocials.user.services.interfaces.PostService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
public class PostController {

    private final CommentService commentService;
    private final FollowerService followerService;
    private final PostService postService;

    public PostController(CommentService commentService, FollowerService followerService, PostService postService) {
        this.commentService = commentService;
        this.followerService = followerService;
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity<CreatePostResponse> createPost(@RequestBody CreatePostRequest createPostRequest) {
        try {
            CreatePostResponse response = postService.createPost(createPostRequest);
            return ResponseEntity.ok(response);
        }catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new CreatePostResponse());
        }
    }

    @PutMapping
    public ResponseEntity<?> editPost(@RequestBody EditPostRequest request) {
        try {
            EditPostResponse response = postService.editPost(request);
            return ResponseEntity.ok(response);
        } catch (SecurityException securityException) {
            return ResponseEntity.status(403).body(securityException.getMessage());
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.badRequest().body(illegalArgumentException.getMessage());
        } catch (Exception exception) {
            return ResponseEntity.internalServerError().body("An unexpected error occurred.");
        }
    }

    @DeleteMapping
    public ResponseEntity<?> deletePost(@RequestBody DeletePostRequest request) {
        try {
            DeletePostResponse response = postService.deletePost(request);
            return ResponseEntity.ok(response);
        } catch (SecurityException securityException) {
            return ResponseEntity.status(403).body(securityException.getMessage());
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.badRequest().body(illegalArgumentException.getMessage());
        } catch (Exception exception) {
            return ResponseEntity.internalServerError().body("An unexpected error occurred.");
        }
    }

    @PostMapping
    public ResponseEntity<?> createComment(@RequestBody CreateCommentRequest createCommentRequest) {
        try {
            CreateCommentResponse createCommentResponse = commentService.createComment(createCommentRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(createCommentResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<?> getCommentsForPost(@PathVariable String postId) {
        List<Comment> comments = commentService.getCommentsForPost(postId);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/comment/{commentId}/replies")
    public ResponseEntity<?> getReplies(@PathVariable String commentId) {
        List<Comment> replies = commentService.getReplies(commentId);
        return ResponseEntity.ok(replies);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable String commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.ok(Map.of("message", "Comment deleted"));
    }

    @PostMapping("/follow")
    public ResponseEntity<?> follow(@RequestParam String followerId, @RequestParam String followingId) {
        followerService.followUser(followerId, followingId);
        return ResponseEntity.ok("Followed successfully");
    }

}
