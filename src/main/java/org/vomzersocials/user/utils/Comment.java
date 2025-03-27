package org.vomzersocials.user.utils;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import org.vomzersocials.user.data.models.User;

import java.util.UUID;

@Entity  // Add missing @Entity annotation
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

    // ... other fields
}
