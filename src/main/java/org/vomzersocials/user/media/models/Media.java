package org.vomzersocials.user.media.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.vomzersocials.user.data.models.User;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "media")
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "CHAR(36)")
    private UUID id; // Changed from String to UUID

    private String caption;

    @Column(nullable = false)
    private String url; // Changed from filePath to url (for cloud/local storage consistency)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaType mediaType; // Now explicitly stored as STRING

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, columnDefinition = "CHAR(36)")
    private User user;
}
