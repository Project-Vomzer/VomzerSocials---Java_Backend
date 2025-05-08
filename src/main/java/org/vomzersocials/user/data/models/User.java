package org.vomzersocials.user.data.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.vomzersocials.user.enums.Role;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;

@Setter
@Getter
@Entity
@Table(name = "users")
public class User{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "CHAR(36)", updatable = false, nullable = false)
    private String id;

    @Column(name = "user_name", nullable = false, unique = true)
    private String userName;
    private String password;
    private Boolean isLoggedIn;

    @Column(nullable = false, unique = true)
    private String suiAddress;
    private String publicKey;

    @Enumerated(EnumType.STRING)
    private Role role;

    @ManyToMany
    @JoinTable(
            name = "user_following",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "following_id")
    )
    private Set<User> following = new HashSet<>();

    @ManyToMany(mappedBy = "following")
    private Set<User> followers = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Like> likes = new HashSet<>();

//    private int likeCount;
    @OneToMany(mappedBy = "user")
    private List<Like>UserLikes;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private  List<Media> mediaList = new ArrayList<>();

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> posts = new ArrayList<>();

    private LocalDateTime dateOfCreation;

}
