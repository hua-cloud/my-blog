package com.example.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.domain.content.model.entity.BlogArticleEntity;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("blog_article")
@AutoMapper(target = BlogArticleEntity.class)
public class BlogArticle extends BasePo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String summary;

    private String contentMarkdown;

    private Long authorId;

    private Long categoryId;

    private Long coverFileId;

    private Integer status;

    private LocalDateTime publishedAt;
}
