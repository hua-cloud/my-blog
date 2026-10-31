package com.example.infrastructure.adapter.repository.content;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.domain.content.adapter.repository.ICategoryRepository;
import com.example.domain.content.model.entity.Category;
import com.example.infrastructure.persistence.mapper.content.IBlogCategoryMapper;
import com.example.infrastructure.persistence.po.CategoryPo;
import com.example.types.core.AppException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;
import static com.example.types.enums.ResultStatus.CATEGORY_NAME_EXISTS;

@Repository
public class CategoryRepository implements ICategoryRepository {
    private final IBlogCategoryMapper mapper;

    public CategoryRepository(IBlogCategoryMapper mapper) { this.mapper = mapper; }

    @Override
    public Optional<Category> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id)).map(this::toDomain);
    }

    @Override
    public Optional<Category> findByIdForUpdate(Long id) {
        return Optional.ofNullable(mapper.selectByIdForUpdate(id)).map(this::toDomain);
    }

    @Override
    public Long create(Category category, Long operatorId) {
        CategoryPo po = toPo(category);
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
        CategoryPo po = toPo(category);
        po.setUpdateBy(operatorId);
        po.setUpdateTime(LocalDateTime.now());
        try {
            mapper.updateById(po);
        } catch (DuplicateKeyException e) {
            throw new AppException(CATEGORY_NAME_EXISTS);
        }
    }

    @Override
    public void delete(Long id) { mapper.deleteById(id); }

    @Override
    public boolean existsByName(String name, Long excludingId) {
        return mapper.selectCount(Wrappers.<CategoryPo>lambdaQuery().eq(CategoryPo::getName, name)
                .ne(excludingId != null, CategoryPo::getId, excludingId)) > 0;
    }

    private Category toDomain(CategoryPo po) {
        return Category.restore(po.getId(), po.getName(), po.getDescription(), po.getSortOrder());
    }

    private CategoryPo toPo(Category category) {
        CategoryPo po = new CategoryPo();
        po.setId(category.getId());
        po.setName(category.getName());
        po.setDescription(category.getDescription());
        po.setSortOrder(category.getSortOrder());
        return po;
    }
}
