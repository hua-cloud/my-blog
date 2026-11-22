package com.example.domain.content.service;

import com.example.domain.content.model.dto.BlogCategoryResponseDTO;
import com.example.domain.content.model.dto.BlogTagResponseDTO;
import com.example.domain.content.model.dto.PublicArticleDetailResponseDTO;
import com.example.domain.content.model.dto.PublicArticleSummaryResponseDTO;
import com.example.types.core.PageQuery;
import com.example.types.core.PageResult;

import java.util.List;

public interface IPublicArticleQueryService {

    PageResult<PublicArticleSummaryResponseDTO> page(String searchKeyword, Long categoryId, Long tagId, PageQuery pageQuery);

    PublicArticleDetailResponseDTO detail(Long id);

    List<BlogCategoryResponseDTO> listCategories();

    List<BlogTagResponseDTO> listTags();
}
