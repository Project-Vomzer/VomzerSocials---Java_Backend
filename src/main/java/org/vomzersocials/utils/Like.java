package org.vomzersocials.utils;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.vomzersocials.data.models.Post;
import org.vomzersocials.user.data.model.User;

@Setter
@Getter
@Entity
public class Like {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Post post;

}
