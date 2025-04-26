package org.vomzersocials.user.data.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "CHAR(36)")
    private String id;

    @ManyToOne
    @JoinColumn(name = "post_id", columnDefinition = "CHAR(36)")
    private Post post;

    @ManyToOne  // Add relationship annotation
    @JoinColumn(name = "user_id", columnDefinition = "CHAR(36)")
    private User user;

}
