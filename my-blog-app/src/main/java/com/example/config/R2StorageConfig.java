package com.example.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.retries.StandardRetryStrategy;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;
import java.time.Duration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(R2StorageProperties.class)
public class R2StorageConfig {

    /** 仅构建客户端，不在启动时访问 R2；关闭客户端时 SDK 同时关闭自有 HTTP transport。 */
    @Bean(destroyMethod = "close")
    public S3Client r2S3Client(R2StorageProperties properties) {
        return S3Client.builder()
                .endpointOverride(URI.create(properties.getEndpoint()))
                .region(Region.of("auto"))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(
                        properties.getAccessKeyId(), properties.getSecretAccessKey())))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .chunkedEncodingEnabled(false)
                        .build())
                .httpClientBuilder(UrlConnectionHttpClient.builder()
                        .connectionTimeout(Duration.ofSeconds(5))
                        .socketTimeout(Duration.ofSeconds(30)))
                .overrideConfiguration(config -> config
                        .apiCallAttemptTimeout(Duration.ofSeconds(30))
                        .apiCallTimeout(Duration.ofSeconds(60))
                        // 条件上传遇到响应丢失时，避免自动重试把结果变成 412。
                        .retryStrategy(StandardRetryStrategy.builder().maxAttempts(1).build()))
                .build();
    }
}
