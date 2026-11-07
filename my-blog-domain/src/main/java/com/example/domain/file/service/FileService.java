package com.example.domain.file.service;

import cn.hutool.core.util.RandomUtil;
import com.example.domain.file.adapter.port.IFileStoragePort;
import com.example.domain.file.adapter.repository.IBlogFileRepository;
import com.example.domain.file.model.entity.BlogFileEntity;
import com.example.domain.file.model.entity.FileUploadSource;
import com.example.domain.file.model.valobj.StorageLocation;
import com.example.types.core.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Locale;

import static com.example.types.constant.FileUploadConstants.KEY_DATE;
import static com.example.types.constant.FileUploadConstants.MAX_KEY_RETRIES;
import static com.example.types.constant.FileUploadConstants.SAFE_EXTENSION;
import static com.example.types.enums.ResultStatus.FILE_STORAGE_KEY_CONFLICT;
import static com.example.types.enums.ResultStatus.FILE_UPLOAD_FAILED;
import static com.example.types.enums.ResultStatus.FILE_UPLOAD_RESULT_UNKNOWN;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService implements IFileService {

    private final IFileStoragePort storagePort;

    private final IBlogFileRepository fileRepository;

    private final Clock clock;

    @Override
    public BlogFileEntity upload(String filename, long fileSize, String mimeType, FileUploadSource.StreamSource streamSource) {
        StorageLocation location = store(filename, fileSize, mimeType, streamSource);
        BlogFileEntity metadata = BlogFileEntity.builder()
                .originalName(filename)
                .storageType(location.storageType().name())
                .bucketName(location.bucketName())
                .storageKey(location.storageKey())
                .mimeType(mimeType)
                .fileSize(fileSize)
                .build();
        metadata.setId(saveMetadata(metadata));
        return metadata;
    }

    private StorageLocation store(String filename, long fileSize, String mimeType, FileUploadSource.StreamSource streamSource) {
        String prefix = "images/" + LocalDate.now(clock).format(KEY_DATE) + "/";
        String suffix = safeSuffix(filename);
        for (int retries = 0; ; retries++) {
            String key = prefix + RandomUtil.randomString(RandomUtil.BASE_CHAR, 6) + suffix;
            try {
                return storagePort.store(new FileUploadSource(key, fileSize, mimeType, streamSource));
            } catch (AppException failure) {
                if (failure.getResultStatus() == FILE_UPLOAD_RESULT_UNKNOWN) {
                    log.warn("对象上传结果待核查，key={}", key);
                }
                if (failure.getResultStatus() != FILE_STORAGE_KEY_CONFLICT || retries >= MAX_KEY_RETRIES) {
                    throw failure;
                }
            }
        }
    }

    private Long saveMetadata(BlogFileEntity metadata) {
        try {
            return fileRepository.save(metadata);
        } catch (RuntimeException cause) {
            log.warn("文件元数据写入失败，bucket={}, key={}", metadata.getBucketName(), metadata.getStorageKey());
            throw new AppException(FILE_UPLOAD_FAILED, cause);
        }
    }

    private static String safeSuffix(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot <= 0) {
            return "";
        }
        String suffix = filename.substring(dot + 1);
        return SAFE_EXTENSION.matcher(suffix).matches() ? "." + suffix.toLowerCase(Locale.ROOT) : "";
    }
}
