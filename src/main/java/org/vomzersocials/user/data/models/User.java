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
//public class User implements UserDetails {
public class User{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(
            columnDefinition = "CHAR(36)",
            updatable = false,
            nullable = false
    )
    private String id;

    @Column(name = "user_name")
    private String userName;
    private String password;
    private Boolean isLoggedIn;
    private String suiAddress;
    private String publicKey;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Media> mediaPosts = new HashSet<>();

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

    private int likeCount;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private  List<Media> mediaList = new ArrayList<>();

    private LocalDateTime dateOfCreation;


//    @Override
//    public Collection<? extends GrantedAuthority> getAuthorities() {
//        return List.of(new SimpleGrantedAuthority(role.name()));
//    }
//
//    @Override
//    public String getUsername() {
//        return this.userName;
//    }
//
//    @Override
//    public boolean isAccountNonExpired() {
////        return UserDetails.super.isAccountNonExpired();
//        return true;
//    }
//
//    @Override
//    public boolean isAccountNonLocked() {
////        return UserDetails.super.isAccountNonLocked();
//        return true;
//    }
//
//    @Override
//    public boolean isCredentialsNonExpired() {
////        return UserDetails.super.isCredentialsNonExpired();
//        return true;
//    }
//
//    @Override
//    public boolean isEnabled() {
////        return UserDetails.super.isEnabled();
//        return this.isLoggedIn;
//    }



}
