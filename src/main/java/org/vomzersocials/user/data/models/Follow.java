package org.vomzersocials.user.data.models;

import jakarta.persistence.*;
import lombok.*;
import org.vomzersocials.user.dtos.requests.FollowUserRequest;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
//@Entity
//public class Follower {
////    private User userId;
//    @EmbeddedId
//    private FollowId id;
//    private Boolean isFollowing;
//}
@Entity
@EqualsAndHashCode
public class Follow {
    @EmbeddedId
    private FollowId id;

    @ManyToOne
    @MapsId("followerId")
    @JoinColumn(name = "follower_id")
    private User follower;

    @ManyToOne
    @MapsId("followingId")
    @JoinColumn(name = "following_id")
    private User following;
    private Boolean isFollowing;
    private LocalDateTime followedAt = LocalDateTime.now();


}

