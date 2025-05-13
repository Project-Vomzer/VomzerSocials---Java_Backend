package org.vomzersocials.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.vomzersocials.user.dtos.requests.*;
import org.vomzersocials.user.dtos.responses.CreatePostResponse;
import org.vomzersocials.user.dtos.responses.DeletePostResponse;
import org.vomzersocials.user.dtos.responses.EditPostResponse;
import org.vomzersocials.user.dtos.responses.RepostResponse;
import org.vomzersocials.user.exceptions.PostNotFoundException;
import org.vomzersocials.user.services.interfaces.PostService;
import org.vomzersocials.user.springSecurity.JwtUtil;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Slf4j
public class PostController {

    private final PostService postService;
    private final JwtUtil jwtUtil;

    private Mono<String> validateAndExtractUserId(String authHeader) {
        return Mono.fromCallable(() -> {
            String token = authHeader.replaceFirst("^Bearer\\s+", "");
            if (!jwtUtil.validateToken(token)) {
                throw new SecurityException("Invalid or expired token");
            }
            return jwtUtil.extractUserId(token);
        });
    }

    @PostMapping("/create")
    public Mono<ResponseEntity<CreatePostResponse>> createPost(
            @RequestBody CreatePostRequest request,
            @RequestHeader("Authorization") String authHeader) {

        return validateAndExtractUserId(authHeader)
                .flatMap(userId -> postService.createPost(request, userId))
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    log.error("Create post error", e);
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(CreatePostResponse.builder()
                                    .errorMessage(e.getMessage())
                                    .build()));
                });
    }

    @PutMapping("/edit")
    public Mono<ResponseEntity<EditPostResponse>> editPost(
            @RequestBody EditPostRequest request,
            @RequestHeader("Authorization") String authHeader) {

        return validateAndExtractUserId(authHeader)
                .flatMap(userId -> postService.editPost(request, userId))
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    HttpStatus status = determineStatus(e);
                    return Mono.just(ResponseEntity.status(status)
                            .body(EditPostResponse.builder()
                                    .errorMessage(e.getMessage())
                                    .build()));
                });
    }

    private HttpStatus determineStatus(Throwable e) {
        if (e instanceof SecurityException) return HttpStatus.FORBIDDEN;
        if (e instanceof PostNotFoundException) return HttpStatus.NOT_FOUND;
        if (e instanceof IllegalArgumentException) return HttpStatus.BAD_REQUEST;
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    @DeleteMapping("/delete")
    public Mono<ResponseEntity<DeletePostResponse>> deletePost(
            @RequestBody DeletePostRequest request,
            @RequestHeader("Authorization") String authHeader) {

        return validateAndExtractUserId(authHeader)
                .flatMap(userId -> postService.deletePost(request, userId))
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    log.error("Delete post error for postId: {}", request.getPostId(), e);
                    HttpStatus status = determineStatus(e);
                    return Mono.just(ResponseEntity.status(status)
                            .body(DeletePostResponse.builder()
                                    .postId(request.getPostId())
                                    .message(e.getMessage())
                                    .build()));
                });
    }

    @PostMapping("/repost")
    public Mono<ResponseEntity<RepostResponse>> repost(
            @RequestBody RepostRequest request,
            @RequestHeader("Authorization") String authHeader) {

        return validateAndExtractUserId(authHeader)
                .flatMap(userId -> postService.repost(request, userId))
                .map(response -> {
//                    log.info("Repost successful for postId: {} by userId: {}", request.getPostId(), userId);
                    return ResponseEntity.ok(response);
                })
                .onErrorResume(e -> {
                    log.error("Repost error for postId: {}", request.getPostId(), e);
                    HttpStatus status = determineStatus(e);
                    return Mono.just(ResponseEntity.status(status)
                            .body(RepostResponse.builder()
                                    .errorMessage(e.getMessage())
                                    .build()));
                });
    }
}