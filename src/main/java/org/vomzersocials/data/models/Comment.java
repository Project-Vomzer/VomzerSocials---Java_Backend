package org.vomzersocials.user.data.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
public class Comment {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME) // Optional style
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(nullable = false, length = 200)
    private String content;

    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @CreationTimestamp
    private LocalDateTime createdAt;

}
