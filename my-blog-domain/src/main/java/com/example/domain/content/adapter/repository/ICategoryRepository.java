package com.example.domain.content.adapter.repository;

import com.example.domain.content.model.entity.Category;
import com.example.domain.content.query.model.CategoryQuery;
import com.example.types.page.PageResult;
import java.util.Optional;

public interface ICategoryRepository {
    Optional<Category> findById(Long id);
    PageResult<Category> page(CategoryQuery query);
    Long create(Category category, Long operatorId);
    void update(Category category, Long operatorId);
    void delete(Long id);
    boolean existsByName(String name, Long excludingId);
}
