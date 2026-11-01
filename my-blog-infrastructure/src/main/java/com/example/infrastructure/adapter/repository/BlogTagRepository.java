package com.example.infrastructure.adapter.repository;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.domain.content.adapter.repository.IBlogTagRepository;
import com.example.domain.content.model.entity.BlogTagEntity;
import com.example.infrastructure.persistence.mapper.IBlogTagMapper;
import com.example.infrastructure.persistence.converter.PageConverter;
import com.example.types.core.PageQuery;
import com.example.types.core.PageResult;
import com.example.infrastructure.persistence.po.BlogTag;
import com.example.types.core.AppException;
import com.example.types.utils.MapstructUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.example.types.enums.ResultStatus.TAG_NAME_EXISTS;

@Repository
@RequiredArgsConstructor
public class BlogTagRepository implements IBlogTagRepository {

    private final IBlogTagMapper tagMapper;

    @Override
    public BlogTagEntity selectById(Long id) {
        BlogTag blogTag = tagMapper.selectById(id);
        if (ObjUtil.isNull(blogTag)) {
            return null;
        }
        return MapstructUtils.convert(blogTag, BlogTagEntity.class);
    }

    @Override
    public PageResult<BlogTagEntity> page(String searchKeyword, PageQuery query) {
        QueryWrapper<BlogTag> qw = Wrappers.query();
        qw.lambda().like(StrUtil.isNotBlank(searchKeyword), BlogTag::getName, searchKeyword);
        if (query.isValidOrder(BlogTag.class)) {
            qw.orderBy(true, query.getIsAsc(), StrUtil.toUnderlineCase(query.getOrderColumn()));
        } else {
            qw.lambda().orderByAsc(BlogTag::getId);
        }
        Page<BlogTag> page = tagMapper.selectPage(PageConverter.toPage(query), qw);
        List<BlogTagEntity> converted = MapstructUtils.convert(page.getRecords(), BlogTagEntity.class);
        return PageConverter.toResult(converted, page.getTotal());
    }

    @Override
    public int save(BlogTagEntity tagEntity) {
        BlogTag converted = MapstructUtils.convert(tagEntity, BlogTag.class);
        try {
            return tagMapper.insert(converted);
        } catch (DuplicateKeyException e) {
            throw new AppException(TAG_NAME_EXISTS);
        }
    }

    @Override
    public int update(BlogTagEntity tagEntity) {
        BlogTag converted = MapstructUtils.convert(tagEntity, BlogTag.class);
        try {
            LambdaUpdateWrapper<BlogTag> luw = Wrappers.lambdaUpdate();
            luw.set(StrUtil.isNotBlank(converted.getName()), BlogTag::getName, converted.getName())
                    .eq(BlogTag::getId, converted.getId());
            return tagMapper.update(new BlogTag(), luw);
        } catch (DuplicateKeyException e) {
            throw new AppException(TAG_NAME_EXISTS);
        }
    }

    @Override
    public int delete(Long id) {
        return tagMapper.deleteById(id);
    }
}
