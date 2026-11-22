package com.example.domain.content.adapter.repository;

import com.example.domain.content.model.entity.BlogCategoryEntity;
import com.example.types.core.PageQuery;
import com.example.types.core.PageResult;

import java.util.List;

public interface IBlogCategoryRepository {

    BlogCategoryEntity selectById(Long id);

    boolean existsById(Long id);

    PageResult<BlogCategoryEntity> page(String searchKeyword, PageQuery query);

    List<BlogCategoryEntity> listWithPublishedArticles();

    int save(BlogCategoryEntity categoryEntity);

    int update(BlogCategoryEntity categoryEntity);

    int delete(Long id);
}
