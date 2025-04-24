package org.vomzersocials.user.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.vomzersocials.user.services.implementations.MediaService;
import org.vomzersocials.user.utils.Media;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/media")
public class MediaController {

    private static final Logger logger = LoggerFactory.getLogger(MediaController.class);

    @Autowired
    private MediaService mediaService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadMedia(@RequestParam("file") MultipartFile file) {
        try {
            logger.info("Uploading file: {}", file.getOriginalFilename());
            Media saved = null;
            try {
                saved = mediaService.uploadMedia(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return ResponseEntity.ok(Map.of(
                    "id", saved.getId(),
                    "type", saved.getMediaType(),
                    "url", saved.getUrl()
            ));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid media type: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid media type", "details", e.getMessage()));
        }
    }

    @GetMapping("/presigned-url")
    public ResponseEntity<?> getPresignedUrl(
            @RequestParam String filename,
            @RequestParam(defaultValue = "images") String folder) {
        try {
            logger.info("Generating presigned URL for file: {} in folder: {}", filename, folder);
            String url = mediaService.generatePresignedUploadUrl(filename, folder);
            return ResponseEntity.ok(Map.of("uploadUrl", url));
        } catch (Exception e) {
            logger.error("Failed to generate presigned URL: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to generate presigned URL", "details", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllMedia(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            // You can add pagination here if needed
            List<Media> mediaList = mediaService.getAll(page, size);

            if (mediaList.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT)
                        .body(Map.of("message", "No media available"));
            }

            return ResponseEntity.ok(mediaList);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch media", "details", e.getMessage()));
        }
    }
}
