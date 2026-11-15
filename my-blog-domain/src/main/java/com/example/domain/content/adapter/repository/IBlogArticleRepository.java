package com.example.domain.content.adapter.repository;

import com.example.domain.content.model.entity.BlogArticleEntity;
import com.example.types.core.PageQuery;
import com.example.types.core.PageResult;

public interface IBlogArticleRepository {

    BlogArticleEntity selectById(Long id);

    PageResult<BlogArticleEntity> page(String searchKeyword, Integer status, PageQuery query);

    int save(BlogArticleEntity article);

    int update(BlogArticleEntity article);

    int delete(Long id);
}
