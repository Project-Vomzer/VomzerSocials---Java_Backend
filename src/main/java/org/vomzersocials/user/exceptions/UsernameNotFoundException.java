package org.vomzersocials.user.exceptions;

public class UsernameNotFoundException extends RuntimeException {
    public UsernameNotFoundException(String string) {
        super(string);
    }
}
