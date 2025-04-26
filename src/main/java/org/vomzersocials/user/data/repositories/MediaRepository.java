package org.vomzersocials.user.data.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.vomzersocials.user.data.models.User;
import org.vomzersocials.user.data.models.Media;
import org.vomzersocials.user.enums.MediaType;

import java.util.List;
import java.util.UUID;

public interface MediaRepository extends JpaRepository<Media, UUID> {
    List<Media> findByUser(User user);
    Page<Media> findByFilenameContainingIgnoreCase(String filename, Pageable pageable);
    Page<Media> findByMediaType(MediaType mediaType, Pageable pageable);
    Page<Media> findByFilenameContainingIgnoreCaseAndMediaType(String filename, MediaType mediaType, Pageable pageable);
    Media deleteMediaByFilename(String filename);
    Media findMediaByFilename(String filename);
}
