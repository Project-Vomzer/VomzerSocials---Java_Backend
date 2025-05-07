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
public class S3Config {

    @Value("${vomzer.walrus.access-key}")
    private String accessKey;

    @Value("${vomzer.walrus.secret-key}")
    private String secretKey;

    @Value("${cloud.aws.region.static}")
    private String region;

    @Value("${cloud.aws.s3.endpoint}")
    private String endpoint;

    @PostConstruct
    public void validateConfig() {
        System.out.println("✅ S3Config loaded with:");
        System.out.println("   Endpoint: " + endpoint);
        System.out.println("   Region: " + region);
        System.out.println("   Access Key: " + (accessKey != null ? "✔️ set" : "❌ missing"));
        System.out.println("   Secret Key: " + (secretKey != null ? "✔️ set" : "❌ missing"));
    }

    private StaticCredentialsProvider credentialsProvider() {
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)
        );
    }

    private URI endpointUri() {
        return URI.create(endpoint);
    }

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .endpointOverride(endpointUri())
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider())
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .endpointOverride(endpointUri())
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider())
                .build();
    }
}
