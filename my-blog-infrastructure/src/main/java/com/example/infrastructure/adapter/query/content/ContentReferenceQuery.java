package com.example.infrastructure.adapter.query.content;

import com.example.domain.content.adapter.query.IContentReferenceQuery;
import com.example.infrastructure.persistence.mapper.content.ICategoryReferenceMapper;
import org.springframework.stereotype.Repository;

@Repository
public class ContentReferenceQuery implements IContentReferenceQuery {
    private final ICategoryReferenceMapper mapper;

    public ContentReferenceQuery(ICategoryReferenceMapper mapper) { this.mapper = mapper; }

    @Override
    public boolean existsArticleUsingCategory(Long categoryId) {
        return mapper.existsArticleUsingCategory(categoryId);
    }
}
