package com.example.domain.content.adapter.repository;

import com.example.domain.content.model.entity.BlogTagEntity;
import com.example.types.core.PageQuery;
import com.example.types.core.PageResult;

import java.util.List;

public interface IBlogTagRepository {

    BlogTagEntity selectById(Long id);

    PageResult<BlogTagEntity> page(String searchKeyword, PageQuery query);

    List<BlogTagEntity> listWithPublishedArticles();

    int save(BlogTagEntity tagEntity);

    int update(BlogTagEntity tagEntity);

    int delete(Long id);
}
