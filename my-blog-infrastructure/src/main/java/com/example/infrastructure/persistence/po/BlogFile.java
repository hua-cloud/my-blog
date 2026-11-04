package com.example.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.domain.file.model.entity.BlogFileEntity;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("blog_file")
@AutoMapper(target = BlogFileEntity.class)
public class BlogFile extends BasePo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String originalName;

    private String storageType;

    private String bucketName;

    private String storageKey;

    private String mimeType;

    private Long fileSize;
}
