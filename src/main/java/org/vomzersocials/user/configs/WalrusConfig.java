package org.vomzersocials.user.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
public class WalrusConfig {

    @Value("${walrus.endpoint}")
    private String endpoint;

    @Value("${walrus.accessKey}")
    private String accessKey;

    @Value("${walrus.secretKey}")
    private String secretKey;

    @Value("${walrus.bucket}")
    private String bucketName;

    @Value("${walrus.cdn-url}")
    private String cdnUrl;

    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of("us-east-1"))  // Specify a region (it can be any region if Walrus ignores it)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)
                ))
                .build();
    }

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of("us-east-1")) // Dummy region; Walrus doesn't enforce this
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)
                ))
                .build();
    }

    @Bean
    public String bucketName() {
        return bucketName;
    }

    @Bean
    public String cdnUrl() {
        return cdnUrl;
    }

}
