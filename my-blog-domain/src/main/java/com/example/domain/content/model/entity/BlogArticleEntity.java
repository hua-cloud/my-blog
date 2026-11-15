package com.example.domain.content.model.entity;

import com.example.domain.content.model.dto.BlogArticleResponseDTO;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AutoMapper(target = BlogArticleResponseDTO.class, reverseConvertGenerate = false)
public class BlogArticleEntity {

    private Long id;

    private String title;

    private String summary;

    private String contentMarkdown;

    private Long authorId;

    private Long categoryId;

    private Long coverFileId;

    private Integer status;

    private LocalDateTime publishedAt;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
