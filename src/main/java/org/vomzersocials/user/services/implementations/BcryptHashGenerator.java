package org.vomzersocials.user.services.implementations;

public class BcryptHashGenerator {
    public static void main(String[] args) {
        String raw = "yourPlainPassword";
        String hash = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode(raw);
        System.out.println("BCrypt hash: " + hash);
    }
}
