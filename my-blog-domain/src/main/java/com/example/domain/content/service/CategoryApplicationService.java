package com.example.domain.content.service;

import com.example.domain.content.adapter.query.ICategoryQuery;
import com.example.domain.content.adapter.query.IContentReferenceQuery;
import com.example.domain.content.adapter.repository.ICategoryRepository;
import com.example.domain.content.service.command.SaveCategoryCommand;
import com.example.domain.content.model.entity.Category;
import com.example.domain.content.query.model.CategoryQuery;
import com.example.domain.content.query.model.CategoryView;
import com.example.types.core.AppException;
import com.example.types.page.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import static com.example.types.enums.ResultStatus.*;

@Service
public class CategoryApplicationService {
    private final ICategoryRepository repository;
    private final IContentReferenceQuery references;
    private final ICategoryQuery queries;

    public CategoryApplicationService(ICategoryRepository repository, IContentReferenceQuery references, ICategoryQuery queries) {
        this.repository = repository;
        this.references = references;
        this.queries = queries;
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public Long create(SaveCategoryCommand command, Long operatorId) {
        requirePositive(operatorId, "操作者ID");
        Objects.requireNonNull(command, "分类内容不能为空");
        Category category = Category.create(command.name(), command.description(), command.sortOrder());
        checkUnique(category.getName(), null);
        return repository.create(category, operatorId);
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void update(Long id, SaveCategoryCommand command, Long operatorId) {
        requirePositive(id, "分类ID");
        requirePositive(operatorId, "操作者ID");
        Objects.requireNonNull(command, "分类内容不能为空");
        Category category = repository.findByIdForUpdate(id).orElseThrow(() -> new AppException(CATEGORY_NOT_FOUND));
        category.change(command.name(), command.description(), command.sortOrder());
        checkUnique(category.getName(), id);
        repository.update(category, operatorId);
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void delete(Long id) {
        requirePositive(id, "分类ID");
        if (repository.findByIdForUpdate(id).isEmpty()) return;
        if (references.existsArticleUsingCategory(id)) throw new AppException(CATEGORY_IN_USE);
        repository.delete(id);
    }

    public CategoryView detail(Long id) {
        requirePositive(id, "分类ID");
        return queries.findDetail(id).orElseThrow(() -> new AppException(CATEGORY_NOT_FOUND));
    }

    public PageResult<CategoryView> page(CategoryQuery query) {
        return queries.page(Objects.requireNonNull(query, "查询条件不能为空"));
    }

    private void checkUnique(String name, Long excludingId) {
        if (repository.existsByName(name, excludingId)) throw new AppException(CATEGORY_NAME_EXISTS);
    }

    private static void requirePositive(Long value, String field) {
        if (value == null || value <= 0) throw new IllegalArgumentException(field + "必须大于0");
    }
}
