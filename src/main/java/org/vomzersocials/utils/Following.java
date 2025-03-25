package org.vomzersocials.utils;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import org.vomzersocials.data.models.User;


@Setter
@Getter
@Entity
public class Following {
    @Id
    @ManyToOne
    @JoinColumn(name = "Followed_User_Id")
    private User id;
    private Boolean isFollowed;
}
