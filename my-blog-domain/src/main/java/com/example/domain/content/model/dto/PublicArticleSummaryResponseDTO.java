package com.example.domain.content.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class PublicArticleSummaryResponseDTO {

    private Long id;

    private String title;

    private String summary;

    private LocalDateTime publishedAt;

    private Long categoryId;

    private String categoryName;

    private String coverUrl;

    private List<BlogTagResponseDTO> tags;
}
