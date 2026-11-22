package com.example.domain.content.model.entity;

import com.example.domain.content.model.dto.BlogArticleResponseDTO;
import com.example.domain.content.model.dto.PublicArticleDetailResponseDTO;
import com.example.domain.content.model.dto.PublicArticleSummaryResponseDTO;
import io.github.linpeilie.annotations.AutoMapper;
import io.github.linpeilie.annotations.AutoMappers;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@AutoMappers({
        @AutoMapper(target = BlogArticleResponseDTO.class, reverseConvertGenerate = false),
        @AutoMapper(target = PublicArticleSummaryResponseDTO.class, reverseConvertGenerate = false),
        @AutoMapper(target = PublicArticleDetailResponseDTO.class, reverseConvertGenerate = false)
})
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

    private String coverStorageKey;

    private Set<Long> tagIds;

    private List<BlogTagEntity> tags;

    private Integer status;

    private LocalDateTime publishedAt;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
