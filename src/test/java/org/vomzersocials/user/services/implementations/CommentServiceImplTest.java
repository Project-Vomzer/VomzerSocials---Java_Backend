package org.vomzersocials.user.services.implementations;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentServiceImpl commentService;

    @Test
    public void createComment_ShouldReturnCreatedComment_test() {
        CreateCommentRequest request = new CreateCommentRequest();
        request.setPostId("validPostId");
        request.setUserId("validUserId");
        request.setContent("Test Comment");

        Post post = new Post();
        post.setId("validPostId");

        User user = new User();
        user.setId("validUserId");

        Comment savedComment = new Comment();
        savedComment.setId(UUID.randomUUID().toString());
        savedComment.setContent(request.getContent());
        savedComment.setUser(user);
        savedComment.setPost(post);
        savedComment.setCreatedAt(LocalDateTime.now());

        when(postRepository.findById(eq("validPostId"))).thenReturn(Optional.of(post));
        when(userRepository.findUserById(request.getUserId())).thenReturn(user);
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        CreateCommentResponse response = commentService.createComment(request);

        assertNotNull(response);
        assertEquals("Test Comment", response.getContent());
        assertEquals("validPostId", response.getPostId());
        assertEquals("validUserId", response.getUserId());
    }

    @Test
    public void createComment_ShouldReturnCreatedCommentResponse_WhenParentCommentExists() {
        String parentId = UUID.randomUUID().toString();

        CreateCommentRequest request = new CreateCommentRequest();
        request.setPostId("validPostId");
        request.setUserId("validUserId");
        request.setContent("Reply Comment");
        request.setParentCommentId(parentId);

        Post post = new Post();
        post.setId("validPostId");

        User user = new User();
        user.setId("validUserId");

        Comment parentComment = new Comment();
        parentComment.setId(parentId);
        parentComment.setContent("Parent Comment");

        Comment savedComment = new Comment();
        savedComment.setId(UUID.randomUUID().toString());
        savedComment.setContent("Reply Comment");
        savedComment.setUser(user);
        savedComment.setPost(post);
        savedComment.setParentComment(parentComment);
        savedComment.setCreatedAt(LocalDateTime.now());

        when(postRepository.findById(anyString())).thenReturn(Optional.of(post));
        when(userRepository.findUserById(request.getUserId())).thenReturn(user);
        when(commentRepository.findById(UUID.fromString(parentId))).thenReturn(Optional.of(parentComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        CreateCommentResponse response = commentService.createComment(request);

        assertNotNull(response);
        assertEquals("Reply Comment", response.getContent());
        assertEquals("validPostId", response.getPostId());
        assertEquals("validUserId", response.getUserId());
    }


    @Test
    public void createComment_ShouldThrowPostNotFoundException_WhenPostNotFound() {
        CreateCommentRequest request = new CreateCommentRequest();
        request.setPostId("invalidPostId");
        request.setUserId("validUserId");
        request.setContent("Test Comment");

        when(postRepository.findById(request.getPostId())).thenReturn(Optional.empty());
        assertThrows(PostNotFoundException.class, () -> commentService.createComment(request));
    }

    @Test
    public void createComment_ShouldThrowUserDoesNotExistException_WhenUserNotFound() {
        CreateCommentRequest request = new CreateCommentRequest();
        request.setPostId("validPostId");
        request.setUserId("invalidUserId");
        request.setContent("Test Comment");

        Post post = new Post();
        post.setId("validPostId");

        when(postRepository.findById(request.getPostId())).thenReturn(Optional.of(post));
        when(userRepository.findUserById(request.getUserId())).thenReturn(null);
        assertThrows(UserDoesNotExistException.class, () -> commentService.createComment(request));
    }
}
