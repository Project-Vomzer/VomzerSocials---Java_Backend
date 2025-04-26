//package org.vomzersocials.user.services.implementations;
//
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.*;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.mock.web.MockMultipartFile;
//import org.vomzersocials.user.data.repositories.MediaRepository;
//import org.vomzersocials.user.data.models.Media;
//import org.vomzersocials.user.enums.MediaType;
//import software.amazon.awssdk.core.sync.RequestBody;
//import software.amazon.awssdk.services.s3.S3Client;
//import software.amazon.awssdk.services.s3.model.PutObjectRequest;
//import software.amazon.awssdk.services.s3.presigner.S3Presigner;
//import org.springframework.data.domain.PageImpl;
//
//import java.io.IOException;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//public class MediaServiceTest {
//
//    @InjectMocks
//    private MediaService mediaService;
//
//    @Mock
//    private MediaRepository mediaRepository;
//
//    @Mock
//    private S3Client s3Client;
//
//    @Mock
//    private S3Presigner s3Presigner;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//        mediaService = new MediaService();
//        mediaService.mediaRepository = mediaRepository;
//        mediaService.s3Client = s3Client;
//        mediaService.s3Presigner = s3Presigner;
//        mediaService.bucketName = "test-bucket";
//        mediaService.cdnUrl = "https://cdn.example.com";
//    }
//
//    @Test
//    public void mediaCanBeUploaded_test() throws IOException {
//        MockMultipartFile file = new MockMultipartFile(
//                "file", "test.jpg", "image/jpeg", "dummy".getBytes());
//
//        Media savedMedia = new Media();
//        savedMedia.setFilename("test.jpg");
//        savedMedia.setMediaType(MediaType.IMAGE);
//        savedMedia.setUrl("https://cdn.example.com/images/test.jpg");
//
//        when(mediaRepository.save(any(Media.class))).thenReturn(savedMedia);
//
//        Media result = mediaService.uploadMedia(file);
//
//        assertEquals("test.jpg", result.getFilename());
//        assertEquals(MediaType.IMAGE, result.getMediaType());
//        assertTrue(result.getUrl().startsWith("https://cdn.example.com/"));
//        verify(s3Client, times(1)).putObject((PutObjectRequest) any(PutObjectRequest.class), (RequestBody) any());
//    }
//
//    @Test
//    public void mediaCanBeSearched_test() {
//        String query = "cat";
//        PageRequest pageRequest = PageRequest.of(0, 10);
//        Media media = new Media();
//        media.setFilename("cat-video.mp4");
//        media.setMediaType(MediaType.VIDEO);
//        media.setUrl("https://cdn.example.com/videos/cat-video.mp4");
//
//        List<Media> mediaList = List.of(media);
//        Page<Media> mediaPage = new PageImpl<>(mediaList, pageRequest, mediaList.size());
//
//        when(mediaRepository.findByFilenameContainingIgnoreCaseAndMediaType(query, media.getMediaType(), pageRequest))
//                .thenReturn(mediaPage);
//
//        Page<Media> result = mediaService.searchForMedia(query, media.getMediaType(), pageRequest);
//
//        assertNotNull(result);
//        assertEquals(1, result.getTotalElements());
//        assertEquals("cat-video.mp4", result.getContent().get(0).getFilename());
//        verify(mediaRepository, times(1))
//                .findByFilenameContainingIgnoreCaseAndMediaType(query, media.getMediaType(), pageRequest);
//    }
//
//    @Test
//    public void searchTermWillReturnExactMatch_test() {
//        String query = "dog";
//        PageRequest pageRequest = PageRequest.of(0, 10);
//        Media media = new Media();
//        media.setFilename("dog.jpg");
//        media.setMediaType(MediaType.IMAGE);
//        media.setUrl("https://cdn.example.com/images/dog.jpg");
//
//        Page<Media> mediaPage = new PageImpl<>(List.of(media), pageRequest, 1);
//
//        when(mediaRepository.findByFilenameContainingIgnoreCaseAndMediaType(query, media.getMediaType(), pageRequest)).thenReturn(mediaPage);
//
//        Page<Media> result = mediaService.searchForMedia(query,media.getMediaType() , pageRequest);
//
//        assertEquals(1, result.getTotalElements());
//        assertEquals("dog.jpg", result.getContent().get(0).getFilename());
//    }
//
//    @Test
//    public void searchTermCanReturnPartialMatch_test() {
//        String query = "cat";
//        PageRequest pageRequest = PageRequest.of(0, 10);
//        Media media = new Media();
//        media.setFilename("funny-cat-video.mp4");
//        media.setMediaType(MediaType.VIDEO);
//        media.setUrl("https://cdn.example.com/videos/funny-cat-video.mp4");
//
//        Page<Media> mediaPage = new PageImpl<>(List.of(media), pageRequest, 1);
//
//        when(mediaRepository.findByFilenameContainingIgnoreCaseAndMediaType(query,media.getMediaType(), pageRequest)).thenReturn(mediaPage);
//
//        Page<Media> result = mediaService.searchForMedia(query,media.getMediaType(), pageRequest);
//
//        assertFalse(result.isEmpty());
//        assertTrue(result.getContent().get(0).getFilename().contains("cat"));
//    }
//
//    @Test
//    public void searchIsCaseInsensitive_test() {
//        String query = "DoG";
//        PageRequest pageRequest = PageRequest.of(0, 10);
//        Media media = new Media();
//        media.setFilename("dog.mp4");
//
//        Page<Media> mediaPage = new PageImpl<>(List.of(media), pageRequest, 1);
//
//        when(mediaRepository.findByFilenameContainingIgnoreCase(query, pageRequest)).thenReturn(mediaPage);
//
//        Page<Media> result = mediaService.searchForMedia(query,media.getMediaType(), pageRequest);
//
//        assertEquals("dog.mp4", result.getContent().get(0).getFilename());
//    }
//
//    @Test
//    public void searchForMediaNotInRepo_returnsNoMatch_test() {
//        String query = "giraffe";
//        PageRequest pageRequest = PageRequest.of(0, 10);
//        MediaType mediaType = MediaType.IMAGE;
//
//        Page<Media> mediaPage = new PageImpl<>(List.of(), pageRequest, 0);
//
//        when(mediaRepository.findByFilenameContainingIgnoreCaseAndMediaType(query, mediaType, pageRequest)).thenReturn(mediaPage);
//
//        Page<Media> result = mediaService.searchForMedia(query, mediaType, pageRequest);
//
//        assertTrue(result.isEmpty());
//    }
//
//    @Test
//    public void searchForMediaWithSimilarTerms_resultReturnsMultipleMediaTerms_test() {
//        String query = "sun";
//        PageRequest pageRequest = PageRequest.of(0, 10);
//
//        Media media1 = new Media();
//        media1.setFilename("sunrise.jpg");
//        media1.setMediaType(MediaType.IMAGE);
//        media1.setUrl("https://cdn.example.com/images/sunrise.jpg");
//
//        Media media2 = new Media();
//        media2.setFilename("sunset.png");
//        media2.setMediaType(MediaType.IMAGE);
//        media2.setUrl("https://cdn.example.com/images/sunset.png");
//
//        Page<Media> mediaPage = new PageImpl<>(List.of(media1, media2), pageRequest, 2);
//
//        when(mediaRepository.findByFilenameContainingIgnoreCaseAndMediaType(query,MediaType.IMAGE, pageRequest)).thenReturn(mediaPage);
//
//        Page<Media> result = mediaService.searchForMedia(query, MediaType.IMAGE, pageRequest);
//
//        assertEquals(2, result.getTotalElements());
//        assertEquals("sunrise.jpg", result.getContent().get(0).getFilename());
//        assertEquals("sunset.png", result.getContent().get(1).getFilename());
//    }
//
////    @Test
////    public void alreadyUploadedMediaCanBeDeletedFromS3_test(){
////        String query = "DoG";
////        PageRequest pageRequest = PageRequest.of(0, 10);
////        Media media = new Media();
////        media.setFilename("dog.mp4");
////
////        Page<Media> mediaPage = new PageImpl<>(List.of(media), pageRequest, 1);
////        when(mediaRepository.findByFilenameContainingIgnoreCase(query, pageRequest)).thenReturn(mediaPage);
////        Page<Media> result = mediaService.searchForMedia(query,media.getMediaType(), pageRequest);
////        assertEquals("dog.mp4", result.getContent().get(0).getFilename());
////
////        Media foundMedia = mediaService.deleteMedia(media.getFilename());
////        assertEquals("dog.mp4", foundMedia.getFilename());
////
////    }
//
//
//}