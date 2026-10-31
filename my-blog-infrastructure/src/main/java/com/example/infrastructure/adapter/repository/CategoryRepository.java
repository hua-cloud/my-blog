package com.example.infrastructure.adapter.repository;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.domain.content.adapter.repository.ICategoryRepository;
import com.example.domain.content.model.entity.Category;
import com.example.infrastructure.persistence.mapper.IBlogCategoryMapper;
import com.example.domain.content.query.model.CategoryQuery;
import com.example.infrastructure.persistence.converter.PageConverter;
import com.example.types.page.PageResult;
import com.example.infrastructure.persistence.po.BlogCategory;
import com.example.types.core.AppException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;
import static com.example.types.enums.ResultStatus.CATEGORY_NAME_EXISTS;
import static com.example.types.enums.ResultStatus.CATEGORY_NOT_FOUND;

@Repository
public class CategoryRepository implements ICategoryRepository {
    private final IBlogCategoryMapper mapper;

    public CategoryRepository(IBlogCategoryMapper mapper) { this.mapper = mapper; }

    @Override
    public Optional<Category> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id)).map(this::toDomain);
    }

    @Override
    public PageResult<Category> page(CategoryQuery query) {
        var wrapper = Wrappers.<BlogCategory>lambdaQuery()
                .apply(query.getKeyword() != null, "LOCATE({0}, name) > 0", query.getKeyword())
                .orderByAsc(BlogCategory::getSortOrder, BlogCategory::getId);
        return PageConverter.toResult(mapper.selectPage(PageConverter.<BlogCategory>toPage(query), wrapper), this::toDomain);
    }

    @Override
    public Long create(Category category, Long operatorId) {
        BlogCategory po = toPo(category);
        LocalDateTime now = LocalDateTime.now();
        po.setCreateBy(operatorId);
        po.setCreateTime(now);
        po.setUpdateBy(operatorId);
        po.setUpdateTime(now);
        try {
            mapper.insert(po);
        } catch (DuplicateKeyException e) {
            throw new AppException(CATEGORY_NAME_EXISTS);
        }
        return po.getId();
    }

    @Override
    public void update(Category category, Long operatorId) {
        BlogCategory po = toPo(category);
        po.setUpdateBy(operatorId);
        po.setUpdateTime(LocalDateTime.now());
        try {
            if (mapper.updateById(po) == 0) throw new AppException(CATEGORY_NOT_FOUND);
        } catch (DuplicateKeyException e) {
            throw new AppException(CATEGORY_NAME_EXISTS);
        }
    }

    @Override
    public void delete(Long id) { mapper.deleteById(id); }

    @Override
    public boolean existsByName(String name, Long excludingId) {
        return mapper.selectCount(Wrappers.<BlogCategory>lambdaQuery().eq(BlogCategory::getName, name)
                .ne(excludingId != null, BlogCategory::getId, excludingId)) > 0;
    }

    private Category toDomain(BlogCategory po) {
        return Category.restore(po.getId(), po.getName(), po.getDescription(), po.getSortOrder());
    }

    private BlogCategory toPo(Category category) {
        BlogCategory po = new BlogCategory();
        po.setId(category.getId());
        po.setName(category.getName());
        po.setDescription(category.getDescription());
        po.setSortOrder(category.getSortOrder());
        return po;
    }
}
