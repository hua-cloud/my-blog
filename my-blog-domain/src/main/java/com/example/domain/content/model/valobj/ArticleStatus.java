package com.example.domain.content.model.valobj;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ArticleStatus {

    DRAFT(0),
    PUBLISHED(1),
    UNPUBLISHED(2);

    private final int code;
}
