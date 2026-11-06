package com.example.domain.file.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/** 已上传并完成元数据落库的文件信息。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponseDTO implements Serializable {

    private Long fileId;

    private String objectKey;

    private String url;

    private String originalName;

    private String mimeType;

    private long fileSize;
}
