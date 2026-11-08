package com.example.domain.file.service;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.RandomUtil;
import com.example.domain.file.adapter.port.IFileStoragePort;
import com.example.domain.file.adapter.repository.IBlogFileRepository;
import com.example.domain.file.model.dto.BlogFileResponseDTO;
import com.example.domain.file.model.entity.BlogFileEntity;
import com.example.domain.file.model.entity.FileUploadSource;
import com.example.domain.file.model.valobj.FileType;
import com.example.domain.file.model.valobj.StorageLocation;
import com.example.domain.file.model.valobj.StorageType;
import com.example.types.core.AppException;
import com.example.types.core.PageQuery;
import com.example.types.core.PageResult;
import com.example.types.utils.MapstructUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadPoolExecutor;

import static com.example.types.constant.Constants.DOT;
import static com.example.types.constant.Constants.SLASH;
import static com.example.types.constant.FileUploadConstants.*;
import static com.example.types.enums.ResultStatus.FILE_UPLOAD_FAILED;
import static com.example.types.enums.ResultStatus.FILE_UPLOAD_RESULT_UNKNOWN;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlogFileService implements IBlogFileService {

    private final IFileStoragePort storagePort;

    private final IBlogFileRepository fileRepository;

    private final ThreadPoolExecutor threadPoolExecutor;

    private final Clock clock;

    @Override
    public PageResult<BlogFileResponseDTO> page(String searchKeyword, FileType fileType, PageQuery pageQuery) {
        PageResult<BlogFileEntity> result = fileRepository.page(searchKeyword, fileType, pageQuery);
        List<BlogFileResponseDTO> records = MapstructUtils.convert(result.getRecords(), BlogFileResponseDTO.class);
        return PageResult.of(records, result.getTotal());
    }

    @Override
    public boolean delete(Long id) {
        BlogFileEntity entity = fileRepository.selectById(id);
        if (ObjUtil.isNull(entity)) {
            return false;
        }

        threadPoolExecutor.execute(() -> {
            try {
                StorageLocation location = new StorageLocation(
                        StorageType.valueOf(entity.getStorageType()), entity.getBucketName(), entity.getStorageKey());
                storagePort.delete(location);
            } catch (Exception e) {
                log.warn("文件对象异步删除失败，id={}, bucket={}, key={}", id, entity.getBucketName(), entity.getStorageKey(), e);
            }
        });
        return true;
    }

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
        entity.setId(saveMetadata(entity, location));
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

    private Long saveMetadata(BlogFileEntity entity, StorageLocation location) {
        try {
            return fileRepository.save(entity);
        } catch (Exception e1) {
            BlogFileEntity existing = fileRepository.selectByStorageLocation(location.storageType(), location.bucketName(), location.storageKey());
            if (ObjUtil.isNotNull(existing)) {
                return existing.getId();
            }
            threadPoolExecutor.execute(() -> {
                try {
                    storagePort.delete(location);
                } catch (RuntimeException e2) {
                    log.warn("文件补偿删除失败，location={}", location, e2);
                }
            });
            throw new AppException(FILE_UPLOAD_FAILED, e1);
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
