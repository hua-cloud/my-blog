package com.example.domain.content.model.entity;

import com.example.domain.content.model.dto.BlogArticleResponseDTO;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@AutoMapper(target = BlogArticleResponseDTO.class, reverseConvertGenerate = false)
public class BlogArticleEntity {

    private Long id;

    private String title;

    private String summary;

    private String contentMarkdown;

    private Long authorId;

    private Long categoryId;

    private String categoryName;

    private Long coverFileId;

    private String coverFileName;

    private Set<Long> tagIds;

    private List<BlogTagEntity> tags;

    private Integer status;

    private LocalDateTime publishedAt;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
