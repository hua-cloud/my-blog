package com.example.domain.file.adapter.repository;

import com.example.domain.file.model.entity.BlogFileEntity;
import com.example.types.core.PageQuery;
import com.example.types.core.PageResult;

public interface IBlogFileRepository {

    /** 按原始文件名、存储类型或桶名模糊搜索并分页查询；默认按 ID 倒序返回。 */
    PageResult<BlogFileEntity> page(String searchKeyword, PageQuery query);

    /**
     * 新增文件元数据，忽略传入实体的 ID，返回数据库生成的主键；失败抛出异常。
     */
    Long save(BlogFileEntity fileEntity);

    /**
     * 按完整存储位置查询，供上传提交结果核查使用；不存在时返回 null。
     */
    BlogFileEntity selectByStorageLocation(String storageType, String bucketName, String storageKey);
}
