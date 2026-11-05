package com.example.domain.file.adapter.port;

import com.example.domain.file.model.entity.FileUploadSource;
import com.example.domain.file.model.valobj.StorageLocation;
import com.example.types.core.AppException;

public interface IFileStoragePort {

    /**
     * 使用调用方提供的对象键上传文件，同步消费并关闭输入流。
     *
     * @param source 文件上传源
     * @return 写入成功后的存储位置
     * @throws AppException 对象键冲突、上传失败或上传结果不确定
     */
    StorageLocation store(FileUploadSource source);

    /**
     * 补偿删除本存储提供方管理的对象，对象已不存在时按成功处理。
     *
     * @param location 待删除对象的存储位置
     * @throws AppException 存储服务删除失败
     */
    void delete(StorageLocation location);
}
