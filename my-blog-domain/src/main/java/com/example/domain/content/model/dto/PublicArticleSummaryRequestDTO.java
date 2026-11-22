package com.example.domain.content.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PublicArticleSummaryRequestDTO {

    private String searchKeyword;

    private Long categoryId;

    private Long tagId;
}
