package org.vomzersocials.utils;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import org.vomzersocials.user.data.model.User;

@Setter
@Getter
@Entity
public class Like {
    @Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "Liker_Id", nullable = false)
    private User id;

}
