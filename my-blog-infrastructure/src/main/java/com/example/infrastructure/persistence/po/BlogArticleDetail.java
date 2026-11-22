package com.example.infrastructure.persistence.po;

import com.example.domain.content.model.entity.BlogArticleEntity;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AutoMapper(target = BlogArticleEntity.class, reverseConvertGenerate = false)
public class BlogArticleDetail extends BlogArticle {

    private String categoryName;

    private String coverFileName;

    private String coverStorageKey;

    private List<BlogTag> tags;
}
