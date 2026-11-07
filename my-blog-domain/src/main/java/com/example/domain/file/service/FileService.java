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

import static com.example.types.constant.Constants.DOT;
import static com.example.types.constant.Constants.SLASH;
import static com.example.types.constant.FileUploadConstants.*;
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
        BlogFileEntity entity = BlogFileEntity.builder()
                .originalName(filename)
                .storageType(location.storageType().name())
                .bucketName(location.bucketName())
                .storageKey(location.storageKey())
                .mimeType(mimeType)
                .fileSize(fileSize)
                .build();
        entity.setId(fileRepository.save(entity));
        return entity;
    }

    private StorageLocation store(String filename, long fileSize, String mimeType, FileUploadSource.StreamSource streamSource) {
        String prefix = IMAGES_DIRECTORY + LocalDate.now(clock).format(KEY_DATE) + SLASH;
        String suffix = safeSuffix(filename);
        String key = prefix + RandomUtil.randomString(RandomUtil.BASE_CHAR_NUMBER, 6) + suffix;
        try {
            return storagePort.store(new FileUploadSource(key, fileSize, mimeType, streamSource));
        } catch (AppException e) {
            if (e.getResultStatus() == FILE_UPLOAD_RESULT_UNKNOWN) {
                log.warn("对象上传结果待核查，key={}", key);
            }
            throw e;
        }
    }

    private String safeSuffix(String filename) {
        int dot = filename.lastIndexOf(DOT);
        if (dot <= 0) {
            return "";
        }
        String suffix = filename.substring(dot + 1);
        return SAFE_EXTENSION.matcher(suffix).matches() ? DOT + suffix.toLowerCase(Locale.ROOT) : "";
    }
}
