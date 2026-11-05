package com.example.domain.file.model.entity;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/** 请求期的文件来源；长度必须等于流的实际字节数，每次打开返回新流，由适配器同步消费并关闭。 */
public record FileUploadSource(String storageKey, long fileSize, String mimeType, StreamSource streamSource) {

    public FileUploadSource {
        Objects.requireNonNull(storageKey, "storageKey");
        Objects.requireNonNull(mimeType, "mimeType");
        Objects.requireNonNull(streamSource, "streamSource");
        if (fileSize < 0) {
            throw new IllegalArgumentException("文件长度不能为负数");
        }
    }

    /**
     * 打开一个从头读取文件内容的输入流。
     *
     * @return 新的输入流，由调用方关闭
     * @throws IOException 无法打开文件流
     */
    public InputStream openStream() throws IOException {
        return streamSource.openStream();
    }

    @FunctionalInterface
    public interface StreamSource {
        /**
         * 打开新的文件输入流。
         *
         * @return 从头读取的输入流
         * @throws IOException 无法打开文件流
         */
        InputStream openStream() throws IOException;
    }
}
