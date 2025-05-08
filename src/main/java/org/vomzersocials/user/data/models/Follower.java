package org.vomzersocials.user.data.models;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
public class Follower {
    @EmbeddedId
    private FollowId id;

    private Boolean isFollowing;
}
