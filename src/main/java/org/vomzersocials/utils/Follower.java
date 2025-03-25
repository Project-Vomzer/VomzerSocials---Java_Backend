package org.vomzersocials.utils;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import org.vomzersocials.data.models.User;

@Entity
@Setter
@Getter
public class Follower {
    @Id
    @ManyToOne
    @JoinColumn(name = "id_id")
    private User id;
    private Boolean isFollowing;
}
