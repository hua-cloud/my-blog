package com.example.domain.content.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class BlogArticleResponseDTO {

    private Long id;

    private String title;

    private String summary;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String contentMarkdown;

    private Long authorId;

    private Long categoryId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String categoryName;

    private Long coverFileId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String coverFileName;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<BlogTagResponseDTO> tags;

    private Integer status;

    private LocalDateTime publishedAt;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
