package org.vomzersocials.user.services.implementations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.vomzersocials.user.utils.Media;
import org.vomzersocials.user.utils.MediaType;
import org.vomzersocials.user.data.repositories.MediaRepository;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
public class MediaService {

    @Autowired
    private MediaRepository mediaRepository;

    @Autowired
    private S3Client s3Client;

    @Autowired
    private S3Presigner s3Presigner; // Add this for presigned URL generation

    @Value("${vomzer.bucket-name}")
    private String bucketName;

    @Value("${vomzer.cdn-url}")
    private String cdnUrl;

    // Fetch all media with pagination
    public List<Media> getAll(int page, int size) {
        // Using Spring Data pagination
        Page<Media> mediaPage = mediaRepository.findAll(PageRequest.of(page, size));
        return mediaPage.getContent();
    }

    // Upload media to S3 (Walrus)
    public Media uploadMedia(MultipartFile file) throws IOException {
        String contentType = file.getContentType();

        // Decide the folder based on media type
        String folder;
        MediaType mediaType;

        if (contentType != null && contentType.startsWith("video")) {
            folder = "videos/";
            mediaType = MediaType.VIDEO;
        } else if (contentType != null && contentType.startsWith("image")) {
            folder = "images/";
            mediaType = MediaType.IMAGE;
        } else {
            throw new IllegalArgumentException("Unsupported media type");
        }

        String originalFilename = file.getOriginalFilename();
        String uniqueKey = folder + UUID.randomUUID() + "-" + originalFilename;

        // Upload to Walrus (S3)
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(uniqueKey)
                .contentType(contentType)
                .build();

        try (InputStream inputStream = file.getInputStream()) {
            s3Client.putObject(putRequest, RequestBody.fromInputStream(inputStream, file.getSize()));
        } catch (S3Exception e) {
            throw new IOException("Failed to upload to S3: " + e.getMessage(), e);
        }

        // Save media metadata
        Media media = new Media();
        media.setFilename(originalFilename);
        media.setMediaType(mediaType);
        media.setUrl(cdnUrl + "/" + uniqueKey);

        return mediaRepository.save(media);
    }

    public String generatePresignedUploadUrl(String originalFilename, String folder) {
        // Generate a unique key for the uploaded file
        String key = folder + "/" + UUID.randomUUID() + "-" + originalFilename;

        // Create the PutObjectRequest to specify the details of the upload
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName) // Your bucket name
                .key(key) // The unique key for the uploaded file
                .contentType("image/jpeg") // You can dynamically set this based on file type
                .build();

        // Generate a presigned URL with an expiration of 10 minutes
        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(
                PresignedPutObjectRequest.builder()
                        .putObjectRequest(objectRequest) // The PutObjectRequest
                        .signatureDuration(Duration.ofMinutes(10)) // URL expiration time
                        .build()
        );

        // Return the URL for the client to use
        return presignedRequest.url().toString();
    }

}
