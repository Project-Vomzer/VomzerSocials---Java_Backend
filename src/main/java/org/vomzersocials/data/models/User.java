package org.vomzersocials.data.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;
    private String userName;
    private String password;
    private Boolean isLoggedIn;

    @Enumerated(EnumType.STRING)
    private Role role;

    // Users Following Others (Many-to-Many Self-Join)
    @ManyToMany
    @JoinTable(
            name = "follower",
            joinColumns = @JoinColumn(name = "follower_id"),  // The user who follows
            inverseJoinColumns = @JoinColumn(name = "following_id") // The user being followed
    )
    private Set<User> following = new HashSet<>();

    // Users Being Followed (Inverse of Following)
    @ManyToMany(mappedBy = "following")
    private Set<User> followers = new HashSet<>();

    // Likes (One User can Like Many Posts)
    @OneToMany(mappedBy = "user")
    private Set<Like> likes = new HashSet<>();

}
