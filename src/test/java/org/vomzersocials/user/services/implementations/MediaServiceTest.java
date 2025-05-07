package org.vomzersocials.user.services.implementations;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import org.vomzersocials.user.data.models.Media;
import org.vomzersocials.user.data.repositories.MediaRepository;
import org.vomzersocials.user.enums.MediaType;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MediaServiceTest {

    @InjectMocks
    private MediaService mediaService;

    @Mock
    private MediaRepository mediaRepository;

    @Mock
    private S3Client s3Client;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Set test values
        String testBucketName = "test-bucket";
        String testCdnUrl = "https://cdn.example.com";

        mediaService = new MediaService(
                mediaRepository,
                s3Client,
                mock(S3Presigner.class),
                testBucketName,
                testCdnUrl
        );
    }

    @Test
    void mediaCanBeUploaded_test() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "dummy".getBytes());

        Media savedMedia = new Media();
        savedMedia.setFilename("test.jpg");
        savedMedia.setMediaType(MediaType.IMAGE);
        savedMedia.setUrl("https://cdn.example.com/images/test.jpg");

        when(mediaRepository.save(any(Media.class))).thenReturn(savedMedia);

        Media result = mediaService.uploadMedia(file);

        assertEquals("test.jpg", result.getFilename());
        assertEquals(MediaType.IMAGE, result.getMediaType());
        assertTrue(result.getUrl().startsWith("https://cdn.example.com/"));
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void searchForExactMatch_test() {
        Media media = new Media();
        media.setFilename("dog.mp4");
        media.setMediaType(MediaType.VIDEO);
        media.setUrl("https://cdn.example.com/videos/dog.mp4");

        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Media> page = new PageImpl<>(List.of(media));

        when(mediaRepository.findByFilenameContainingIgnoreCaseAndMediaType("dog", MediaType.VIDEO, pageRequest)).thenReturn(page);

        Page<Media> result = mediaService.searchForMedia("dog", MediaType.VIDEO, pageRequest);

        assertEquals(1, result.getTotalElements());
        assertEquals("dog.mp4", result.getContent().get(0).getFilename());
    }

    @Test
    void searchForPartialMatch_test() {
        Media media = new Media();
        media.setFilename("funny-cat.mp4");
        media.setMediaType(MediaType.VIDEO);

        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Media> page = new PageImpl<>(List.of(media));

        when(mediaRepository.findByFilenameContainingIgnoreCaseAndMediaType("cat", MediaType.VIDEO, pageRequest)).thenReturn(page);

        Page<Media> result = mediaService.searchForMedia("cat", MediaType.VIDEO, pageRequest);

        assertFalse(result.isEmpty());
        assertTrue(result.getContent().get(0).getFilename().contains("cat"));
    }

    @Test
    void searchIsCaseInsensitive_test() {
        Media media = new Media();
        media.setFilename("DOG.mp4");
        media.setMediaType(MediaType.VIDEO);

        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Media> page = new PageImpl<>(List.of(media));

        when(mediaRepository.findByFilenameContainingIgnoreCaseAndMediaType("DoG", MediaType.VIDEO, pageRequest)).thenReturn(page);

        Page<Media> result = mediaService.searchForMedia("DoG", MediaType.VIDEO, pageRequest);

        assertEquals("DOG.mp4", result.getContent().get(0).getFilename());
    }

    @Test
    void searchReturnsNoMatch_test() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Media> page = new PageImpl<>(List.of());

        when(mediaRepository.findByFilenameContainingIgnoreCaseAndMediaType("giraffe", MediaType.IMAGE, pageRequest)).thenReturn(page);

        Page<Media> result = mediaService.searchForMedia("giraffe", MediaType.IMAGE, pageRequest);

        assertTrue(result.isEmpty());
    }

    @Test
    void searchReturnsMultipleResults_test() {
        Media media1 = new Media();
        media1.setFilename("sunrise.jpg");
        media1.setMediaType(MediaType.IMAGE);

        Media media2 = new Media();
        media2.setFilename("sunset.png");
        media2.setMediaType(MediaType.IMAGE);

        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Media> page = new PageImpl<>(List.of(media1, media2));

        when(mediaRepository.findByFilenameContainingIgnoreCaseAndMediaType("sun", MediaType.IMAGE, pageRequest)).thenReturn(page);

        Page<Media> result = mediaService.searchForMedia("sun", MediaType.IMAGE, pageRequest);

        assertEquals(2, result.getTotalElements());
        assertEquals("sunrise.jpg", result.getContent().get(0).getFilename());
        assertEquals("sunset.png", result.getContent().get(1).getFilename());
    }

    @Test
    void alreadyUploadedMediaCanBeDeleted_test() {
        String filename = "delete-me.jpg";
        Media media = new Media();
        media.setFilename(filename);
        media.setMediaType(MediaType.IMAGE);
        media.setUrl("https://cdn.example.com/images/delete-me.jpg");

        when(mediaRepository.findByFilename(filename)).thenReturn(Optional.of(media));

        Media deleted = mediaService.deleteMediaByFilename(filename);

        assertEquals(filename, deleted.getFilename());

        ArgumentCaptor<DeleteObjectRequest> captor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3Client, times(1)).deleteObject(captor.capture());

        DeleteObjectRequest actualRequest = captor.getValue();
        assertEquals("test-bucket", actualRequest.bucket());
        assertEquals("image/delete-me.jpg", actualRequest.key());

        verify(mediaRepository, times(1)).delete(media);
    }

    @Test
    void deleteNonExistentMedia_throwsException_test() {
        when(mediaRepository.findByFilename("nonexistent.jpg")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> mediaService.deleteMediaByFilename("nonexistent.jpg"));
    }


}
