package org.vomzersocials.user.data.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.vomzersocials.utils.Like;
import org.vomzersocials.utils.Role;

import java.util.*;

@Setter
@Getter
@Entity
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME) // Optional style
    @Column(columnDefinition = "CHAR(36)")
    private UUID id;
    @Column(name = "user_name")
    private String userName;
    private String password;
    private Boolean isLoggedIn;
    private String suiAddress;

    @Enumerated(EnumType.STRING)
    private Role role;

    // Users Following Others (Many-to-Many Self-Join)
    @ManyToMany
    @JoinTable(
            name = "user_following",
            joinColumns = @JoinColumn(name = "user_id"),           // The owner’s join column
            inverseJoinColumns = @JoinColumn(name = "following_id")  // The join column for the followed user
    )
    private Set<User> following = new HashSet<>();

    // Users Being Followed (Inverse of Following)
    @ManyToMany(mappedBy = "following")
    private Set<User> followers = new HashSet<>();

    // Likes (One User can Like Many Posts)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Like> likes = new HashSet<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getUsername() {
        return "";
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }



}
