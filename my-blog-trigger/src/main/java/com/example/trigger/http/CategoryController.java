package com.example.trigger.http;

import com.example.api.content.dto.CategoryQueryRequestDTO;
import com.example.api.content.dto.CategoryResponseDTO;
import com.example.api.content.dto.SaveCategoryRequestDTO;
import com.example.api.response.R;
import com.example.domain.content.service.CategoryApplicationService;
import com.example.domain.content.service.command.SaveCategoryCommand;
import com.example.domain.content.query.model.CategoryQuery;
import com.example.domain.content.query.model.CategoryView;
import com.example.trigger.http.security.LoginHelper;
import com.example.types.page.PageResult;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/categories")
public class CategoryController extends BaseController {
    private final CategoryApplicationService service;

    public CategoryController(CategoryApplicationService service) { this.service = service; }

    @PostMapping
    public R<Long> create(@Valid @RequestBody SaveCategoryRequestDTO request) {
        return success(service.create(toCommand(request), LoginHelper.getUserId()));
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
        return new SaveCategoryCommand(request.name(), request.description(), request.sortOrder());
    }

    private CategoryResponseDTO toDTO(CategoryView view) {
        return new CategoryResponseDTO(view.id(), view.name(), view.description(), view.sortOrder());
    }
}
