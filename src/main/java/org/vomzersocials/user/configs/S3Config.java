package org.vomzersocials.user.configs;

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
public class S3Config {

    @Value("${vomzer.walrus.access-key}")
    private String accessKey;

    @Value("${vomzer.walrus.secret-key}")
    private String secretKey;

    @Value("${vomzer.walrus.region}")
    private String region;

    @Value("${vomzer.walrus.endpoint}")
    private String endpoint;

    private StaticCredentialsProvider credentialsProvider() {
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)
        );
    }

    private URI endpointUri() {
        return URI.create(endpoint);
    }

    @Bean(name = "awsS3Client")
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider())
                .endpointOverride(endpointUri())
                .build();
    }

    @Bean(name = "awsPresigner")
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider())
                .endpointOverride(endpointUri())
                .build();
    }
}
