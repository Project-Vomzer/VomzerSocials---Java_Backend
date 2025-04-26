package org.vomzersocials.user.data.models;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_likes")  // Remove apostrophe from table name
@Getter @Setter
public class Like {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "CHAR(36)")
    private String id;

    @ManyToOne
    @JoinColumn(name = "user_id", columnDefinition = "CHAR(36)")
    private User user;

    @ManyToOne
    @JoinColumn(name = "post_id", columnDefinition = "CHAR(36)")
    private Post post;
}
