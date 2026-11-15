package com.example.trigger.http;

import com.example.domain.content.model.dto.BlogArticleRequestDTO;
import com.example.domain.content.model.dto.BlogArticleResponseDTO;
import com.example.domain.content.model.entity.BlogArticleEntity;
import com.example.domain.content.service.BlogArticleService;
import com.example.trigger.http.security.LoginHelper;
import com.example.types.core.PageQuery;
import com.example.types.core.PageResult;
import com.example.types.core.R;
import com.example.types.utils.MapstructUtils;
import com.example.types.valid.AddGroup;
import com.example.types.valid.UpdateGroup;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/admin/article")
@RequiredArgsConstructor
public class BlogArticleController {

    private final BlogArticleService articleService;

    @GetMapping("/{id}")
    public R<BlogArticleResponseDTO> detail(@PathVariable("id") Long id) {
        BlogArticleEntity article = articleService.detail(id);
        return R.success(MapstructUtils.convert(article, BlogArticleResponseDTO.class));
    }

    @GetMapping("/page")
    public R<PageResult<BlogArticleResponseDTO>> page(BlogArticleRequestDTO requestDTO, PageQuery pageQuery) {
        return R.success(articleService.page(requestDTO.getSearchKeyword(), requestDTO.getStatus(), pageQuery));
    }

    @PostMapping
    public R<Void> create(@Validated(AddGroup.class) @RequestBody BlogArticleRequestDTO requestDTO) {
        BlogArticleEntity article = MapstructUtils.convert(requestDTO, BlogArticleEntity.class);
        article.setAuthorId(LoginHelper.getUserId());
        return R.operation(articleService.create(article));
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable("id") Long id,
                          @Validated(UpdateGroup.class) @RequestBody BlogArticleRequestDTO requestDTO) {
        BlogArticleEntity article = MapstructUtils.convert(requestDTO, BlogArticleEntity.class);
        article.setId(id);
        return R.operation(articleService.update(article));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable("id") Long id) {
        return R.operation(articleService.delete(id));
    }
}
