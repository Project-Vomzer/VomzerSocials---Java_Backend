package org.vomzersocials.user.services.implementations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.vomzersocials.user.utils.Media;
import org.vomzersocials.user.utils.MediaType;
import org.vomzersocials.user.data.repositories.MediaRepository;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.UUID;

@Service
public class MediaService {

    @Autowired
    MediaRepository mediaRepository;

    @Autowired
    S3Client s3Client;

    @Autowired
    S3Presigner s3Presigner;

    @Value("${vomzer.bucket-name}")
    String bucketName;

    @Value("${vomzer.cdn-url}")
    String cdnUrl;


    public Page<Media> searchForMedia(String search, MediaType mediaType, Pageable pageable) {
        if (search != null && mediaType != null) {
            return mediaRepository
                    .findByFilenameContainingIgnoreCaseAndMediaType(search, mediaType, pageable);
        } else if (search != null) {
            return mediaRepository
                    .findByFilenameContainingIgnoreCase(search, pageable);
        } else if (mediaType != null) {
            return mediaRepository
                    .findByMediaType(mediaType, pageable);
        } else {
            return mediaRepository.findAll(pageable);
        }
    }


    public Media uploadMedia(MultipartFile file) throws IOException {
        String contentType = file.getContentType();

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

        Media media = new Media();
        media.setFilename(originalFilename);
        media.setMediaType(mediaType);
        media.setUrl(cdnUrl + "/" + uniqueKey);

        return mediaRepository.save(media);
    }

    public String generatePreSignedUploadUrl(String originalFilename, String folder, String contentType) {
        String key = folder + "/" + UUID.randomUUID() + "-" + originalFilename;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest preSignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest preSignedRequest = s3Presigner.presignPutObject(preSignRequest);

        return preSignedRequest.url().toString();
    }

//    public Media deleteMedia(String filename) {
//        if (filename != null) {
//            throw new IllegalArgumentException("No filename provided");
//        }
//        Media foundMedia = mediaRepository.findMediaByFilename(filename);
//        if (foundMedia == null) {
//            throw new IllegalArgumentException("Media already exists");
//        }
//
//    }
}
