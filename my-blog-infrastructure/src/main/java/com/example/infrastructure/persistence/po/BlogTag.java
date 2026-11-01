package com.example.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import com.example.domain.content.model.entity.BlogTagEntity;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("blog_tag")
@AutoMapper(target = BlogTagEntity.class)
public class BlogTag extends BasePo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String color;
}
