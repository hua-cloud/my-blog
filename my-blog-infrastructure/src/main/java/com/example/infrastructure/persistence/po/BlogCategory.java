package com.example.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import com.example.domain.content.model.entity.BlogCategoryEntity;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("blog_category")
@AutoMapper(target = BlogCategoryEntity.class)
public class BlogCategory extends BasePo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String description;

    private Integer sortOrder;
}
