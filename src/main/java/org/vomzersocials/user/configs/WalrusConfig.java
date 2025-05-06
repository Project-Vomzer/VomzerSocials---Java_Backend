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

    @Bean(name = "walrusPresigner")
    public S3Presigner walrusPresigner() {
        return S3Presigner.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of("us-east-1"))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)
                ))
                .build();
    }

    @Bean(name = "walrusS3Client")
    public S3Client walrusS3Client() {
        return S3Client.builder()
                .region(Region.of("us-east-1"))
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)
                ))
                .build();
    }

    @Bean
    public String walrusBucketName() {
        return bucketName;
    }

    @Bean
    public String walrusCdnUrl() {
        return cdnUrl;
    }
}
