package org.vomzersocials.user.exceptions;

public class OwnershipException extends SecurityException {
    public OwnershipException(String userId, String postId) {
        super("User " + userId + " does not own post " + postId);
    }
}