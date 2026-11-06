package com.example.domain.file.model.dto;

/** 已上传并完成元数据落库的文件信息。 */
public record FileUploadResult(Long fileId, String objectKey, String url, String originalName,
                               String mimeType, long fileSize) {
}
