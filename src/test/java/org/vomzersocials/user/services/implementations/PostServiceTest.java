package org.vomzersocials.user.services.implementations;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.vomzersocials.user.data.models.Post;
import org.vomzersocials.user.data.models.User;
import org.vomzersocials.user.data.repositories.PostRepository;
import org.vomzersocials.user.data.repositories.UserRepository;
import org.vomzersocials.user.dtos.requests.CreatePostRequest;
import org.vomzersocials.user.dtos.responses.CreatePostResponse;
import org.vomzersocials.user.enums.Role;
import org.vomzersocials.user.services.implementations.PostService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class PostServiceTest {

    @Autowired
    private PostService postService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserName("testUser");
        testUser.setPassword("pass");
        testUser.setRole(Role.SUBSCRIBER);
        userRepository.save(testUser);
    }

    @Test
    void testCreatePost() {
        CreatePostRequest request = new CreatePostRequest();
        request.setAuthor(userRepository.findUserById(testUser.getId()));
        request.setContent("Integration test post");

        CreatePostResponse post = postService.createPost(request);

        assertNotNull(post.getId());
        assertEquals("Integration test post", post.getContent());
        assertEquals(testUser.getId(), post.getAuthor());
    }
}
