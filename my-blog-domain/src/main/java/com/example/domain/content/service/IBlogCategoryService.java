package com.example.domain.content.service;

import com.example.domain.content.model.dto.BlogCategoryResponseDTO;
import com.example.domain.content.model.entity.BlogCategoryEntity;
import com.example.types.core.PageQuery;
import com.example.types.core.PageResult;

public interface IBlogCategoryService {

    BlogCategoryEntity detail(Long id);

    PageResult<BlogCategoryResponseDTO> page(String searchKeyword, PageQuery pageQuery);

    boolean create(BlogCategoryEntity categoryEntity);

    boolean update(BlogCategoryEntity categoryEntity);

    boolean delete(long id);
}
