package org.vomzersocials.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.vomzersocials.utils.Like;

import java.util.List;
@Getter
@Setter
public class Comment {
    @Id
    private User id;
    private Post post;
    private List<Like> likes;
    private Post replies;

}
