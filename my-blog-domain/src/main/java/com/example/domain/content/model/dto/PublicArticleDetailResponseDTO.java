package com.example.domain.content.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PublicArticleDetailResponseDTO extends PublicArticleSummaryResponseDTO {

    private String contentMarkdown;
}
