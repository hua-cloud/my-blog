package com.example.infrastructure.adapter.query.content;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.domain.content.adapter.query.ICategoryQuery;
import com.example.domain.content.query.model.CategoryQuery;
import com.example.domain.content.query.model.CategoryView;
import com.example.infrastructure.persistence.converter.PageConverter;
import com.example.infrastructure.persistence.mapper.content.IBlogCategoryMapper;
import com.example.infrastructure.persistence.po.CategoryPo;
import com.example.types.page.PageResult;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public class CategoryQueryAdapter implements ICategoryQuery {
    private final IBlogCategoryMapper mapper;

    public CategoryQueryAdapter(IBlogCategoryMapper mapper) { this.mapper = mapper; }

    @Override
    public Optional<CategoryView> findDetail(Long id) {
        return Optional.ofNullable(mapper.selectById(id)).map(this::toView);
    }

    @Override
    public PageResult<CategoryView> page(CategoryQuery query) {
        var wrapper = Wrappers.<CategoryPo>lambdaQuery()
                .apply(query.getKeyword() != null, "LOCATE({0}, name) > 0", query.getKeyword())
                .orderByAsc(CategoryPo::getSortOrder, CategoryPo::getId);
        return PageConverter.toResult(mapper.selectPage(PageConverter.<CategoryPo>toPage(query), wrapper), this::toView);
    }

    private CategoryView toView(CategoryPo po) {
        return new CategoryView(po.getId(), po.getName(), po.getDescription(), po.getSortOrder());
    }
}
