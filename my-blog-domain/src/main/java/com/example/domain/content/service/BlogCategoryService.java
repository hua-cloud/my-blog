package com.example.domain.content.service;

import com.example.domain.content.adapter.repository.ICategoryRepository;
import com.example.domain.content.service.command.SaveCategoryCommand;
import com.example.domain.content.model.entity.Category;
import com.example.domain.content.query.model.CategoryQuery;
import com.example.domain.content.query.model.CategoryView;
import com.example.types.core.AppException;
import com.example.types.page.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import static com.example.types.enums.ResultStatus.*;

@Service
@RequiredArgsConstructor
public class BlogCategoryService {

    private final ICategoryRepository categoryRepository;

    @Transactional(rollbackFor = Exception.class)
    public Long create(SaveCategoryCommand command, Long operatorId) {
        requirePositive(operatorId, "操作者ID");
        Objects.requireNonNull(command, "分类内容不能为空");
        Category category = Category.create(command.getName(), command.getDescription(), command.getSortOrder());
        checkUnique(category.getName(), null);
        return categoryRepository.create(category, operatorId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, SaveCategoryCommand command, Long operatorId) {
        requirePositive(id, "分类ID");
        requirePositive(operatorId, "操作者ID");
        Objects.requireNonNull(command, "分类内容不能为空");
        Category category = categoryRepository.findById(id).orElseThrow(() -> new AppException(CATEGORY_NOT_FOUND));
        category.change(command.getName(), command.getDescription(), command.getSortOrder());
        checkUnique(category.getName(), id);
        categoryRepository.update(category, operatorId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        requirePositive(id, "分类ID");
        categoryRepository.delete(id);
    }

    public CategoryView detail(Long id) {
        requirePositive(id, "分类ID");
        return categoryRepository.findById(id).map(this::toView).orElseThrow(() -> new AppException(CATEGORY_NOT_FOUND));
    }

    public PageResult<CategoryView> page(CategoryQuery query) {
        return categoryRepository.page(Objects.requireNonNull(query, "查询条件不能为空")).map(this::toView);
    }

    private CategoryView toView(Category category) {
        return new CategoryView(category.getId(), category.getName(), category.getDescription(), category.getSortOrder());
    }

    private void checkUnique(String name, Long excludingId) {
        if (categoryRepository.existsByName(name, excludingId)) throw new AppException(CATEGORY_NAME_EXISTS);
    }

    private static void requirePositive(Long value, String field) {
        if (value == null || value <= 0) throw new IllegalArgumentException(field + "必须大于0");
    }
}
