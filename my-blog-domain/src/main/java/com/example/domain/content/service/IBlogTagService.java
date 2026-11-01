package com.example.domain.content.service;

import com.example.domain.content.model.dto.BlogTagResponseDTO;
import com.example.domain.content.model.entity.BlogTagEntity;
import com.example.types.core.PageQuery;
import com.example.types.core.PageResult;

public interface IBlogTagService {

    BlogTagEntity detail(Long id);

    PageResult<BlogTagResponseDTO> page(String searchKeyword, PageQuery pageQuery);

    boolean create(BlogTagEntity tagEntity);

    boolean update(BlogTagEntity tagEntity);

    boolean delete(long id);
}
