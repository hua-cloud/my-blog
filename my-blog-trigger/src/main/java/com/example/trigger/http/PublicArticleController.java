package com.example.trigger.http;

import com.example.domain.content.model.dto.BlogCategoryResponseDTO;
import com.example.domain.content.model.dto.BlogTagResponseDTO;
import com.example.domain.content.model.dto.PublicArticleDetailResponseDTO;
import com.example.domain.content.model.dto.PublicArticleSummaryRequestDTO;
import com.example.domain.content.model.dto.PublicArticleSummaryResponseDTO;
import com.example.domain.content.service.PublicArticleQueryService;
import com.example.types.core.PageQuery;
import com.example.types.core.PageResult;
import com.example.types.core.R;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/public")
@RequiredArgsConstructor
public class PublicArticleController {

    private final PublicArticleQueryService articleQueryService;

    @GetMapping("/article/page")
    public R<PageResult<PublicArticleSummaryResponseDTO>> page(PublicArticleSummaryRequestDTO requestDTO, PageQuery pageQuery) {
        return R.success(articleQueryService.page(
                requestDTO.getSearchKeyword(), requestDTO.getCategoryId(), requestDTO.getTagId(), pageQuery));
    }

    @GetMapping("/article/{id}")
    public R<PublicArticleDetailResponseDTO> detail(@PathVariable("id") @Positive Long id) {
        return R.success(articleQueryService.detail(id));
    }

    @GetMapping("/category/list")
    public R<List<BlogCategoryResponseDTO>> listCategories() {
        return R.success(articleQueryService.listCategories());
    }

    @GetMapping("/tag/list")
    public R<List<BlogTagResponseDTO>> listTags() {
        return R.success(articleQueryService.listTags());
    }
}
