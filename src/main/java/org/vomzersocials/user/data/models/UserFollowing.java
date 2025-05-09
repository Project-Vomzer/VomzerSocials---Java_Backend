package org.vomzersocials.user.data.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@EqualsAndHashCode
public class UserFollowing {
    @Id
    private String id;
    @ManyToOne
    private User follower;

    @ManyToOne
    private User following;
    private Boolean isFollowing;
    private LocalDateTime followedAt = LocalDateTime.now();

}

