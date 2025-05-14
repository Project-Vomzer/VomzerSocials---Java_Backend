package org.vomzersocials.user.controller;

import jakarta.validation.Valid;
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
import org.vomzersocials.user.services.interfaces.PostService;
import org.vomzersocials.user.springSecurity.JwtUtil;
import reactor.core.publisher.Mono;



@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Slf4j
public class PostController {

    private final PostService postService;
    private final JwtUtil jwtService;


    @PostMapping("/create")
    public Mono<ResponseEntity<CreatePostResponse>> createPost(
            @Valid @RequestBody CreatePostRequest request,
            @RequestHeader("Authorization") String authHeader) {

        return validateAndExtractUserId(authHeader)
                .flatMap(userId -> postService.createPost(request, userId))
                .map(ResponseEntity::ok)
                .onErrorResume(exception -> {
                    log.error("Create post error", exception);
                    HttpStatus status = determineStatus(exception);
                    return Mono.just(ResponseEntity.status(status)
                            .body(CreatePostResponse.builder()
                                    .errorMessage(exception.getMessage())
                                    .build()));
                });
    }

    @PutMapping("/edit")
    public Mono<ResponseEntity<EditPostResponse>> editPost(
            @Valid @RequestBody EditPostRequest request,
            @RequestHeader("Authorization") String authHeader) {

        return validateAndExtractUserId(authHeader)
                .flatMap(userId -> postService.editPost(request, userId))
                .map(ResponseEntity::ok)
                .onErrorResume(exception -> {
                    log.error("Edit post error for postId: {}", request.getPostId(), exception);
                    HttpStatus status = determineStatus(exception);
                    return Mono.just(ResponseEntity.status(status)
                            .body(EditPostResponse.builder()
                                    .errorMessage(exception.getMessage())
                                    .build()));
                });
    }

    private HttpStatus determineStatus(Throwable exception) {
        if (exception instanceof IllegalArgumentException) {
            return HttpStatus.BAD_REQUEST;
        } else if (exception instanceof SecurityException) {
            return HttpStatus.UNAUTHORIZED;
        } else {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    private Mono<String> validateAndExtractUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Mono.error(new SecurityException("Invalid or missing Authorization header"));
        }
        String token = authHeader.substring(7);
        try {
            String userId = jwtService.extractUsername(token);
            return Mono.just(userId);
        } catch (Exception e) {
            return Mono.error(new SecurityException("Invalid JWT token"));
        }
    }

    @DeleteMapping("/delete")
    public Mono<ResponseEntity<DeletePostResponse>> deletePost(
            @Valid @RequestBody DeletePostRequest request,
            @RequestHeader("Authorization") String authHeader) {

        return validateAndExtractUserId(authHeader)
                .flatMap(userId -> postService.deletePost(request, userId))
                .map(ResponseEntity::ok)
                .onErrorResume(exception -> {
                    log.error("Delete post error for postId: {}", request.getPostId(), exception);
                    HttpStatus status = determineStatus(exception);
                    return Mono.just(ResponseEntity.status(status)
                            .body(DeletePostResponse.builder()
                                    .postId(request.getPostId())
                                    .message(exception.getMessage())
                                    .build()));
                });
    }

    @PostMapping("/repost")
    public Mono<ResponseEntity<RepostResponse>> repost(
            @Valid @RequestBody RepostRequest request,
            @RequestHeader("Authorization") String authHeader) {

        //                    log.info("Repost successful for postId: {} by userId: {}", request.getPostId(), userId);
        return validateAndExtractUserId(authHeader)
                .flatMap(userId -> postService.repost(request, userId))
                .map(ResponseEntity::ok)
                .onErrorResume(exception -> {
                    log.error("Repost error for postId: {}", request.getPostId(), exception);
                    HttpStatus status = determineStatus(exception);
                    return Mono.just(ResponseEntity.status(status)
                            .body(RepostResponse.builder()
                                    .errorMessage(exception.getMessage())
                                    .build()));
                });
    }
}