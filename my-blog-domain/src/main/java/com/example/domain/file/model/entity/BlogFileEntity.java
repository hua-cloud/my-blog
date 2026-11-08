package com.example.domain.file.model.entity;

import com.example.domain.file.model.dto.BlogFileResponseDTO;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 文件元数据；存储键不包含桶名、域名或临时签名。 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@AutoMapper(target = BlogFileResponseDTO.class, reverseConvertGenerate = false)
public class BlogFileEntity {

    private Long id;

    private String originalName;

    private String storageType;

    private String bucketName;

    private String storageKey;

    private String mimeType;

    private Long fileSize;
}
