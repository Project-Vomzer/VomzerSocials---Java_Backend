//package org.vomzersocials.user.configs;
//
//import com.amazonaws.auth.AWSStaticCredentialsProvider;
//import com.amazonaws.auth.BasicAWSCredentials;
//import com.amazonaws.regions.Region;
//import com.amazonaws.regions.Regions;
//import com.amazonaws.services.s3.AmazonS3;
//import com.amazonaws.services.s3.AmazonS3ClientBuilder;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.PropertySource;
//
//@Configuration
//@PropertySource("classpath:application.properties")
//public class AwsConfig {
//
//    @Value("${aws.accessKeyId}")
//    private String accessKeyId;
//
//    @Value("${aws.secretKey}")
//    private String secretKey;
//
//    @Value("${aws.region}")
//    private String region; // Inject the AWS region
//
//    @Value("${aws.s3.endpoint}")
//    private String s3Endpoint; // Inject the S3 endpoint URL (if using a custom endpoint like Walrus)
//
//    @Bean
//    public AmazonS3 amazonS3() {
//        // Create AWS credentials
//        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKeyId, secretKey);
//
//        // Build the AmazonS3 client
//        return AmazonS3ClientBuilder.standard()
//                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
//                .withRegion(Regions.fromName(region)) // Set the region from properties
//                .applyEndpointConfiguration(new AmazonS3ClientBuilder.EndpointConfiguration(s3Endpoint, region)) // Optional if using a custom endpoint
//                .build();
//    }
//}
