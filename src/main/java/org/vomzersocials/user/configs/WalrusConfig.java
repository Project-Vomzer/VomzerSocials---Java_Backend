package org.vomzersocials.user.configs;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
public class WalrusConfig {

    @Value("${vomzer.walrus.access-key}")
    private String accessKey;

    @Value("${vomzer.walrus.secret-key}")
    private String secretKey;

    @Value("${vomzer.endpoint:http://localhost:9000}")
    private String endpoint;

    @Value("${vomzer.bucket-name}")
    private String bucketName;

    @Value("${vomzer.cdn-url}")
    private String cdnUrl;

    @PostConstruct
    public void sanityCheck() {
        System.out.println("→ vomzer.endpoint = [" + endpoint + "]");
        if (endpoint == null || !endpoint.matches("^[a-zA-Z]+://.*")) {
            throw new IllegalStateException(
                    "Invalid vomzer.endpoint: \"" + endpoint + "\" — must be a full URL with scheme (http:// or https://)");
        }
    }

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
