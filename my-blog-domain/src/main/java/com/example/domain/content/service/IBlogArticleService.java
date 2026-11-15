package com.example.domain.content.service;

import com.example.domain.content.model.dto.BlogArticleResponseDTO;
import com.example.domain.content.model.entity.BlogArticleEntity;
import com.example.types.core.PageQuery;
import com.example.types.core.PageResult;

public interface IBlogArticleService {

    BlogArticleEntity detail(Long id);

    PageResult<BlogArticleResponseDTO> page(String searchKeyword, Integer status, PageQuery pageQuery);

    boolean create(BlogArticleEntity article);

    boolean update(BlogArticleEntity article);

    boolean delete(Long id);
}
