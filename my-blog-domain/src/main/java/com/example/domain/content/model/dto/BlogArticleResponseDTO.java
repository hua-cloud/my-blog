package com.example.domain.content.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BlogArticleResponseDTO {

    private Long id;

    private String title;

    private String summary;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String contentMarkdown;

    private Long authorId;

    private Long categoryId;

    private Long coverFileId;

    private Integer status;

    private LocalDateTime publishedAt;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
