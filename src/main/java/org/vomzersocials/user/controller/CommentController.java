package org.vomzersocials.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.vomzersocials.user.data.models.Comment;
import org.vomzersocials.user.dtos.requests.CreateCommentRequest;
import org.vomzersocials.user.dtos.responses.CreateCommentResponse;
import org.vomzersocials.user.services.interfaces.CommentService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

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
}
