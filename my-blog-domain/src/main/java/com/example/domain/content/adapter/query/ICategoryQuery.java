package com.example.domain.content.adapter.query;

import com.example.domain.content.query.model.CategoryQuery;
import com.example.domain.content.query.model.CategoryView;
import com.example.types.page.PageResult;
import java.util.Optional;

public interface ICategoryQuery {
    Optional<CategoryView> findDetail(Long id);
    PageResult<CategoryView> page(CategoryQuery query);
}
