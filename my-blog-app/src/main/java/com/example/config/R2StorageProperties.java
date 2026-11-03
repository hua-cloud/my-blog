package com.example.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** R2 S3 接入配置 */
@Getter
@Setter
@ConfigurationProperties(prefix = "file.storage.r2")
public class R2StorageProperties {

    private String endpoint;

    private String accessKeyId;

    private String secretAccessKey;

    private String bucket;
}
