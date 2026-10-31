package com.example.domain.content.adapter.repository;

import com.example.domain.content.model.entity.BlogCategoryEntity;
import com.example.types.core.PageQuery;
import com.example.types.core.PageResult;

public interface ICategoryRepository {

    BlogCategoryEntity selectById(Long id);

    PageResult<BlogCategoryEntity> page(String searchKeyword, PageQuery query);

    int save(BlogCategoryEntity categoryEntity);

    int update(BlogCategoryEntity categoryEntity);

    int delete(Long id);
}
