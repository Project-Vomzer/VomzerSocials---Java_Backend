package org.vomzersocials.data.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @OneToOne(cascade = CascadeType.ALL)
    private User author;

    private int likes;

    private String content;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Post> comments;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
