package com.example.domain.file.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogFileResponseDTO implements Serializable {

    private Long id;

    private String originalName;

    private String storageType;

    private String bucketName;

    private String storageKey;

    private String mimeType;

    private Long fileSize;
}
