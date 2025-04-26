package org.vomzersocials.user.data.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Getter
@Setter
public class Follower {
    @EmbeddedId
    private FollowerId id;

    private Boolean isFollowing;

    @Embeddable
    @Getter @Setter
    public static class FollowerId implements Serializable {
        @Column(columnDefinition = "CHAR(36)")
        private String followerId;

        @Column(columnDefinition = "CHAR(36)")
        private String followingId;

    }
}