package org.vomzersocials.utils;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import org.vomzersocials.user.data.models.User;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "user_likes")
public class Like {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME) // Optional style
    @Column(columnDefinition = "CHAR(36)")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
