package com.hackovation.menu_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class AWSConfig {

    @Value("${spring.cloud.aws.s3.access-key}")
    private String awsAccessKey;
    @Value("${spring.cloud.aws.s3.secret-key}")
    private String awsSecretKey;
    @Value("${spring.cloud.aws.region.static}")
    private String awsRegion;

    @Bean
    public S3Client s3Client() {
        System.out.println(awsAccessKey);
        System.out.println(awsSecretKey);
        System.out.println(awsRegion);
        return S3Client.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(awsAccessKey, awsSecretKey)))
                .build();
    }

}
