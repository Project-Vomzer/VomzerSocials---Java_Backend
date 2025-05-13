package org.vomzersocials.user.exceptions;

public class PostNotFoundException extends RuntimeException {
    public PostNotFoundException(String postId) {
        super("Post not found with ID: " + postId);
    }
}