package com.example.trigger.http;

import com.example.api.dto.CategoryQueryRequestDTO;
import com.example.api.dto.CategoryResponseDTO;
import com.example.api.dto.SaveCategoryRequestDTO;
import com.example.api.response.R;
import com.example.domain.content.service.BlogCategoryService;
import com.example.domain.content.service.command.SaveCategoryCommand;
import com.example.domain.content.query.model.CategoryQuery;
import com.example.domain.content.query.model.CategoryView;
import com.example.trigger.http.security.LoginHelper;
import com.example.types.page.PageResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/category")
@RequiredArgsConstructor
public class BlogCategoryController extends BaseController {

    private final BlogCategoryService service;

    @PostMapping
    public R<Void> create(@Valid @RequestBody SaveCategoryRequestDTO requestDTO) {
        return success(service.create(toCommand(requestDTO), LoginHelper.getUserId()));
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable("id") Long id, @Valid @RequestBody SaveCategoryRequestDTO request) {
        service.update(id, toCommand(request), LoginHelper.getUserId());
        return success();
    }

    @GetMapping("/{id}")
    public R<CategoryResponseDTO> detail(@PathVariable("id") Long id) {
        return success(toDTO(service.detail(id)));
    }

    @GetMapping
    public R<PageResult<CategoryResponseDTO>> page(@ModelAttribute CategoryQueryRequestDTO request) {
        CategoryQuery query = new CategoryQuery();
        query.setPageNum(request.getPageNum());
        query.setPageSize(request.getPageSize());
        query.setKeyword(request.getKeyword());
        return success(service.page(query).map(this::toDTO));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable("id") Long id) {
        service.delete(id);
        return success();
    }

    private SaveCategoryCommand toCommand(SaveCategoryRequestDTO request) {
        return new SaveCategoryCommand(request.getName(), request.getDescription(), request.getSortOrder());
    }

    private CategoryResponseDTO toDTO(CategoryView view) {
        return new CategoryResponseDTO(view.getId(), view.getName(), view.getDescription(), view.getSortOrder());
    }
}
