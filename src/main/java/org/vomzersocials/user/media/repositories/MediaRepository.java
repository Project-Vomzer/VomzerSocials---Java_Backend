package org.vomzersocials.user.media.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.vomzersocials.user.data.models.User;
import org.vomzersocials.user.media.models.Media;

import java.util.List;
import java.util.UUID;

public interface MediaRepository extends JpaRepository<Media, UUID> {
    List<Media> findByUser(User user);

}
