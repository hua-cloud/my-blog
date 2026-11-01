package com.example.trigger.http;

import com.example.domain.content.model.dto.BlogTagRequestDTO;
import com.example.domain.content.model.dto.BlogTagResponseDTO;
import com.example.domain.content.model.entity.BlogTagEntity;
import com.example.domain.content.service.BlogTagService;
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
@RequestMapping("/admin/tag")
@RequiredArgsConstructor
public class BlogTagController {

    private final BlogTagService tagService;

    @GetMapping("/{id}")
    public R<BlogTagResponseDTO> detail(@PathVariable("id") Long id) {
        BlogTagEntity entity = tagService.detail(id);
        BlogTagResponseDTO converted = MapstructUtils.convert(entity, BlogTagResponseDTO.class);
        return R.success(converted);
    }

    @GetMapping("/page")
    public R<PageResult<BlogTagResponseDTO>> page(BlogTagRequestDTO requestDTO, PageQuery pageQuery) {
        return R.success(tagService.page(requestDTO.getSearchKeyword(), pageQuery));
    }

    @PostMapping
    public R<Void> create(@Validated(AddGroup.class) @RequestBody BlogTagRequestDTO requestDTO) {
        BlogTagEntity converted = MapstructUtils.convert(requestDTO, BlogTagEntity.class);
        return R.operation(tagService.create(converted));
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable("id") Long id, @Validated(UpdateGroup.class) @RequestBody BlogTagRequestDTO requestDTO) {
        BlogTagEntity converted = MapstructUtils.convert(requestDTO, BlogTagEntity.class);
        converted.setId(id);
        return R.operation(tagService.update(converted));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable("id") Long id) {
        return R.operation(tagService.delete(id));
    }
}
