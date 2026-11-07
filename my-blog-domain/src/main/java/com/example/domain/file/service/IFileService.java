package com.example.domain.file.service;

import com.example.domain.file.model.entity.BlogFileEntity;
import com.example.domain.file.model.entity.FileUploadSource;

public interface IFileService {

    /**
     * 上传已校验的文件，保存元数据后返回结果。
     *
     * @param filename 清理并校验后的文件名
     * @param fileSize 文件实际字节数
     * @param mimeType 根据内容探测得到的 MIME 类型
     * @param streamSource 每次打开均可从头读取的文件源
     * @return 已保存的文件实体，包含数据库生成的主键
     */
    BlogFileEntity upload(String filename, long fileSize, String mimeType, FileUploadSource.StreamSource streamSource);
}
