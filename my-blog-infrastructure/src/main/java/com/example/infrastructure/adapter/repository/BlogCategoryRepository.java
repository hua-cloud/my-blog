package com.example.infrastructure.adapter.repository;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.domain.content.adapter.repository.IBlogCategoryRepository;
import com.example.domain.content.model.entity.BlogCategoryEntity;
import com.example.infrastructure.persistence.mapper.IBlogCategoryMapper;
import com.example.infrastructure.persistence.converter.PageConverter;
import com.example.types.core.PageQuery;
import com.example.types.core.PageResult;
import com.example.infrastructure.persistence.po.BlogCategory;
import com.example.types.core.AppException;
import com.example.types.utils.MapstructUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.example.types.enums.ResultStatus.CATEGORY_NAME_EXISTS;

@Repository
@RequiredArgsConstructor
public class BlogCategoryRepository implements IBlogCategoryRepository {

    private final IBlogCategoryMapper categoryMapper;

    @Override
    public BlogCategoryEntity selectById(Long id) {
        BlogCategory blogCategory = categoryMapper.selectById(id);
        if (ObjUtil.isNull(blogCategory)) {
            return null;
        }
        return MapstructUtils.convert(blogCategory, BlogCategoryEntity.class);
    }

    @Override
    public PageResult<BlogCategoryEntity> page(String searchKeyword, PageQuery query) {
        QueryWrapper<BlogCategory> qw = Wrappers.query();
        qw.and(StrUtil.isNotBlank(searchKeyword), w -> w.lambda()
                .like(BlogCategory::getName, searchKeyword)
                .or()
                .like(BlogCategory::getDescription, searchKeyword)
        );
        if (query.isValidOrder(BlogCategory.class)) {
            qw.orderBy(true, query.getIsAsc(), StrUtil.toUnderlineCase(query.getOrderColumn()));
        } else {
            qw.lambda().orderByAsc(BlogCategory::getSortOrder);
        }
        Page<BlogCategory> page = categoryMapper.selectPage(PageConverter.toPage(query), qw);
        List<BlogCategoryEntity> converted = MapstructUtils.convert(page.getRecords(), BlogCategoryEntity.class);
        return PageConverter.toResult(converted, page.getTotal());
    }

    @Override
    public int save(BlogCategoryEntity categoryEntity) {
        BlogCategory converted = MapstructUtils.convert(categoryEntity, BlogCategory.class);
        try {
            return categoryMapper.insert(converted);
        } catch (DuplicateKeyException e) {
            throw new AppException(CATEGORY_NAME_EXISTS);
        }
    }

    @Override
    public int update(BlogCategoryEntity categoryEntity) {
        BlogCategory converted = MapstructUtils.convert(categoryEntity, BlogCategory.class);
        try {
            LambdaUpdateWrapper<BlogCategory> luw = Wrappers.lambdaUpdate();
            luw.set(StrUtil.isNotBlank(converted.getName()), BlogCategory::getName, converted.getName())
                    .set(ObjUtil.isNotNull(converted.getDescription()), BlogCategory::getDescription, converted.getDescription())
                    .set(ObjUtil.isNotNull(converted.getSortOrder()), BlogCategory::getSortOrder, converted.getSortOrder())
                    .eq(BlogCategory::getId, converted.getId());
            return categoryMapper.update(new BlogCategory(), luw);
        } catch (DuplicateKeyException e) {
            throw new AppException(CATEGORY_NAME_EXISTS);
        }
    }

    @Override
    public int delete(Long id) {
        return categoryMapper.deleteById(id);
    }
}
