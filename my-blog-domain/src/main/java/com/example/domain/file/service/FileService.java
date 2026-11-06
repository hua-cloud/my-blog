package com.example.domain.file.service;

import com.example.domain.file.adapter.port.FileTypePolicy;
import com.example.domain.file.adapter.port.IFileStoragePort;
import com.example.domain.file.adapter.repository.IBlogFileRepository;
import com.example.domain.file.model.dto.FileUploadResult;
import com.example.domain.file.model.entity.BlogFileEntity;
import com.example.domain.file.model.entity.FileUploadSource;
import com.example.domain.file.model.valobj.StorageLocation;
import com.example.types.core.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

import static com.example.types.enums.ResultStatus.FILE_STORAGE_KEY_CONFLICT;
import static com.example.types.enums.ResultStatus.FILE_UPLOAD_FAILED;
import static com.example.types.enums.ResultStatus.FILE_UPLOAD_RESULT_UNKNOWN;

@Slf4j
@Service
public class FileService {

    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;
    private static final int MAX_FILENAME_LENGTH = 255;
    private static final int MAX_KEY_RETRIES = 2;
    private static final String DEFAULT_MIME_TYPE = "application/octet-stream";
    private static final Pattern SAFE_EXTENSION = Pattern.compile("[a-zA-Z0-9]{1,16}");
    private static final DateTimeFormatter KEY_DATE = DateTimeFormatter.ofPattern("uuuu/MM/dd", Locale.ROOT);

    private final IFileStoragePort storage;
    private final IBlogFileRepository repository;
    private final FileTypePolicy typePolicy;
    private final TransactionTemplate writeTransaction;
    private final TransactionTemplate verificationTransaction;
    private final Clock clock;

    public FileService(IFileStoragePort storage, IBlogFileRepository repository, FileTypePolicy typePolicy,
                       PlatformTransactionManager transactionManager, Clock clock) {
        this.storage = storage;
        this.repository = repository;
        this.typePolicy = typePolicy;
        this.clock = clock;
        this.writeTransaction = new TransactionTemplate(transactionManager);
        writeTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.verificationTransaction = new TransactionTemplate(transactionManager);
        verificationTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        // 在 InnoDB 事务内使用锁定读，等待尚未结束的写入，避免因快照漏读而误删对象。
        verificationTransaction.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);
        verificationTransaction.setTimeout(5);
    }

    /**
     * 校验文件并上传到对象存储，确认元数据提交后返回结果。
     *
     * @param originalName 上传时的原始文件名
     * @param fileSize 文件实际字节数
     * @param streamSource 每次打开均可从头读取的文件源
     * @return 已上传文件的元数据和站点访问路径
     * @throws IllegalArgumentException 文件输入不合法
     * @throws IllegalStateException 调用方已开启数据库事务
     * @throws AppException 对象存储或元数据写入失败
     */
    public FileUploadResult upload(String originalName, long fileSize, FileUploadSource.StreamSource streamSource) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("文件上传必须在数据库事务外调用");
        }
        if (fileSize <= 0 || streamSource == null) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        if (fileSize > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("单个文件大小不能超过 5 MB");
        }
        String filename = cleanFilename(originalName);
        String mimeType = detectMimeType(streamSource);
        typePolicy.validate(filename, mimeType);

        StorageLocation location = store(filename, fileSize, mimeType, streamSource);
        BlogFileEntity metadata = BlogFileEntity.builder()
                .originalName(filename)
                .storageType(location.storageType().name())
                .bucketName(location.bucketName())
                .storageKey(location.storageKey())
                .mimeType(mimeType)
                .fileSize(fileSize)
                .build();
        Long fileId = saveMetadata(metadata, location);
        return new FileUploadResult(fileId, location.storageKey(), "/" + location.storageKey(),
                filename, mimeType, fileSize);
    }

    private StorageLocation store(String filename, long fileSize, String mimeType,
                                  FileUploadSource.StreamSource streamSource) {
        String prefix = "file/" + LocalDate.now(clock).format(KEY_DATE) + "/";
        String suffix = safeSuffix(filename);
        for (int retries = 0; ; retries++) {
            String key = prefix + UUID.randomUUID().toString().replace("-", "") + suffix;
            try {
                return storage.store(new FileUploadSource(key, fileSize, mimeType, streamSource));
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

    private Long saveMetadata(BlogFileEntity metadata, StorageLocation location) {
        WriteOutcome outcome = new WriteOutcome();
        try {
            return writeTransaction.execute(status -> {
                TransactionSynchronizationManager.registerSynchronization(outcome);
                outcome.insertStarted = true;
                return Objects.requireNonNull(repository.save(metadata), "文件元数据未返回主键");
            });
        } catch (RuntimeException cause) {
            AppException failure = new AppException(FILE_UPLOAD_FAILED, cause);
            if (outcome.insertStarted && !outcome.rolledBack) {
                Long confirmedId;
                try {
                    confirmedId = verificationTransaction.execute(status -> {
                        BlogFileEntity existing = repository.selectByStorageLocation(metadata.getStorageType(),
                                location.bucketName(), location.storageKey());
                        return existing == null ? null : Objects.requireNonNull(existing.getId(), "文件元数据缺少主键");
                    });
                } catch (RuntimeException verificationFailure) {
                    failure.addSuppressed(verificationFailure);
                    log.warn("文件元数据提交结果待核查，storageType={}, bucket={}, key={}",
                            location.storageType(), location.bucketName(), location.storageKey());
                    throw failure;
                }
                if (confirmedId != null) {
                    return confirmedId;
                }
            }
            try {
                storage.delete(location);
            } catch (RuntimeException deleteFailure) {
                failure.addSuppressed(deleteFailure);
                log.warn("文件上传补偿删除失败，storageType={}, bucket={}, key={}",
                        location.storageType(), location.bucketName(), location.storageKey());
            }
            throw failure;
        }
    }

    private static String cleanFilename(String originalName) {
        if (originalName == null) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        int lastSeparator = Math.max(originalName.lastIndexOf('/'), originalName.lastIndexOf('\\'));
        String filename = originalName.substring(lastSeparator + 1).replaceAll("\\p{Cntrl}", "").strip();
        if (filename.isEmpty() || ".".equals(filename) || "..".equals(filename)) {
            throw new IllegalArgumentException("文件名不合法");
        }
        if (filename.codePointCount(0, filename.length()) > MAX_FILENAME_LENGTH) {
            throw new IllegalArgumentException("文件名不能超过 255 个字符");
        }
        return filename;
    }

    private static String safeSuffix(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot <= 0) {
            return "";
        }
        String suffix = filename.substring(dot + 1);
        return SAFE_EXTENSION.matcher(suffix).matches() ? "." + suffix.toLowerCase(Locale.ROOT) : "";
    }

    private static String detectMimeType(FileUploadSource.StreamSource streamSource) {
        try (var stream = new BufferedInputStream(streamSource.openStream())) {
            String mimeType = URLConnection.guessContentTypeFromStream(stream);
            return mimeType == null ? DEFAULT_MIME_TYPE : mimeType;
        } catch (IOException | RuntimeException ignored) {
            return DEFAULT_MIME_TYPE;
        }
    }

    private static class WriteOutcome implements TransactionSynchronization {
        private boolean insertStarted;
        private boolean rolledBack;

        /**
         * 记录事务管理器确认的回滚状态。
         *
         * @param status 事务完成状态
         */
        @Override
        public void afterCompletion(int status) {
            rolledBack = status == STATUS_ROLLED_BACK;
        }
    }
}
