package com.example.domain.file.adapter.port;

@FunctionalInterface
public interface FileTypePolicy {

    /**
     * 校验文件是否符合上传类型策略。
     *
     * @param originalName 清理后的原始文件名
     * @param mimeType 根据内容识别的 MIME 类型
     */
    void validate(String originalName, String mimeType);
}
