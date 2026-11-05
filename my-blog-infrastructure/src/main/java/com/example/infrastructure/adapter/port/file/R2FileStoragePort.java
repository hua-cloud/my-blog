package com.example.infrastructure.adapter.port.file;

import cn.hutool.core.util.ObjUtil;
import com.example.domain.file.adapter.port.IFileStoragePort;
import com.example.domain.file.model.entity.FileUploadSource;
import com.example.domain.file.model.valobj.StorageLocation;
import com.example.types.core.AppException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.ContentStreamProvider;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Objects;

import static com.example.domain.file.model.valobj.StorageType.R2;
import static com.example.types.enums.ResultStatus.FILE_DELETE_FAILED;
import static com.example.types.enums.ResultStatus.FILE_STORAGE_KEY_CONFLICT;
import static com.example.types.enums.ResultStatus.FILE_UPLOAD_FAILED;
import static com.example.types.enums.ResultStatus.FILE_UPLOAD_RESULT_UNKNOWN;

@Component
public class R2FileStoragePort implements IFileStoragePort {

    private final S3Client s3Client;

    private final String bucketName;

    public R2FileStoragePort(S3Client s3Client, @Value("${file.storage.r2.bucket}") String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    @Override
    public StorageLocation store(FileUploadSource source) {
        Objects.requireNonNull(source, "source");
        InputStream opened;
        try {
            opened = Objects.requireNonNull(source.openStream(), "文件输入流不能为空");
        } catch (IOException | RuntimeException e) {
            throw new AppException(FILE_UPLOAD_FAILED, e);
        }

        try (UploadStreamProvider streams = new UploadStreamProvider(source, opened)) {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(source.storageKey())
                    .contentType(source.mimeType())
                    .contentLength(source.fileSize())
                    .ifNoneMatch("*")
                    .build();
            s3Client.putObject(request, RequestBody.fromContentProvider(streams, source.fileSize(), source.mimeType()));
        } catch (S3Exception e) {
            if (e.statusCode() == 412) {
                throw new AppException(FILE_STORAGE_KEY_CONFLICT, e);
            }
            boolean uncertain = e.statusCode() >= 500 || e.statusCode() == 408 || e.statusCode() == 409;
            throw new AppException(uncertain ? FILE_UPLOAD_RESULT_UNKNOWN : FILE_UPLOAD_FAILED, e);
        } catch (SdkClientException | IOException | UncheckedIOException e) {
            // 网络/读流错误或成功写入后的关闭失败，都不能证明远端对象不存在。
            throw new AppException(FILE_UPLOAD_RESULT_UNKNOWN, e);
        }
        return new StorageLocation(R2, bucketName, source.storageKey());
    }

    @Override
    public void delete(StorageLocation location) {
        Objects.requireNonNull(location, "location");
        if (location.storageType() != R2 || !Objects.equals(bucketName, location.bucketName())) {
            throw new IllegalArgumentException("存储位置不属于当前R2存储桶");
        }
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(location.bucketName())
                    .key(location.storageKey())
                    .build());
        } catch (S3Exception e) {
            if (e.statusCode() == 404 && ObjUtil.isNotNull(e.awsErrorDetails()) && "NoSuchKey".equals(e.awsErrorDetails().errorCode())) {
                return;
            }
            throw new AppException(FILE_DELETE_FAILED, e);
        } catch (SdkClientException e) {
            throw new AppException(FILE_DELETE_FAILED, e);
        }
    }

    /** SDK 即使不重试请求也可能为签名重读内容；每次提供新流，避免依赖 mark/reset 缓冲。 */
    private static class UploadStreamProvider implements ContentStreamProvider, AutoCloseable {
        private final FileUploadSource source;
        private InputStream stream;
        private boolean first = true;

        private UploadStreamProvider(FileUploadSource source, InputStream opened) {
            this.source = source;
            this.stream = opened;
        }

        /**
         * 首次使用已打开的流，SDK 重读时关闭旧流并重新打开。
         *
         * @return 从头读取的输入流
         * @throws UncheckedIOException 无法关闭旧流或重新打开输入流
         */
        @Override
        public InputStream newStream() {
            if (first) {
                first = false;
                return stream;
            }
            try {
                close();
                stream = Objects.requireNonNull(source.openStream(), "文件输入流不能为空");
                return stream;
            } catch (IOException e) {
                throw new UncheckedIOException("文件输入流重新打开失败", e);
            }
        }

        /**
         * 关闭当前输入流。
         *
         * @throws IOException 关闭输入流失败
         */
        @Override
        public void close() throws IOException {
            if (stream != null) {
                InputStream previous = stream;
                stream = null;
                previous.close();
            }
        }
    }
}
