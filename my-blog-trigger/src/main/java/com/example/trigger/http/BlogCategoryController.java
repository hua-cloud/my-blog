package com.example.trigger.http;

import com.example.domain.content.model.dto.BlogCategoryRequestDTO;
import com.example.domain.content.model.dto.BlogCategoryResponseDTO;
import com.example.domain.content.model.entity.BlogCategoryEntity;
import com.example.domain.content.service.BlogCategoryService;
import com.example.types.core.R;
import com.example.types.core.PageQuery;
import com.example.types.core.PageResult;
import com.example.types.utils.MapstructUtils;
import com.example.types.valid.AddGroup;
import com.example.types.valid.UpdateGroup;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/admin/category")
@RequiredArgsConstructor
public class BlogCategoryController {

    private final BlogCategoryService categoryService;

    @GetMapping("/{id}")
    public R<BlogCategoryResponseDTO> detail(@PathVariable("id") Long id) {
        BlogCategoryEntity entity = categoryService.detail(id);
        BlogCategoryResponseDTO converted = MapstructUtils.convert(entity, BlogCategoryResponseDTO.class);
        return R.success(converted);
    }

    @GetMapping("/page")
    public R<PageResult<BlogCategoryResponseDTO>> page(BlogCategoryRequestDTO requestDTO, PageQuery pageQuery) {
        return R.success(categoryService.page(requestDTO.getSearchKeyword(), pageQuery));
    }

    @PostMapping
    public R<Void> create(@Validated(AddGroup.class) @RequestBody BlogCategoryRequestDTO requestDTO) {
        BlogCategoryEntity converted = MapstructUtils.convert(requestDTO, BlogCategoryEntity.class);
        return R.operation(categoryService.create(converted));
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable("id") Long id, @Validated(UpdateGroup.class) @RequestBody BlogCategoryRequestDTO requestDTO) {
        BlogCategoryEntity converted = MapstructUtils.convert(requestDTO, BlogCategoryEntity.class);
        converted.setId(id);
        return R.operation(categoryService.update(converted));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable("id") Long id) {
        return R.operation(categoryService.delete(id));
    }
}
