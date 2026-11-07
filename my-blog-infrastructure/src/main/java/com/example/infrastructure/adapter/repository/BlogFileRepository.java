package com.example.infrastructure.adapter.repository;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.domain.file.adapter.repository.IBlogFileRepository;
import com.example.domain.file.model.entity.BlogFileEntity;
import com.example.domain.file.model.valobj.StorageType;
import com.example.infrastructure.persistence.mapper.IBlogFileMapper;
import com.example.infrastructure.persistence.converter.PageConverter;
import com.example.infrastructure.persistence.po.BlogFile;
import com.example.types.core.PageQuery;
import com.example.types.core.PageResult;
import com.example.types.utils.MapstructUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class BlogFileRepository implements IBlogFileRepository {

    private final IBlogFileMapper fileMapper;

    @Override
    public PageResult<BlogFileEntity> page(String searchKeyword, PageQuery query) {
        QueryWrapper<BlogFile> qw = Wrappers.query();
        qw.and(StrUtil.isNotBlank(searchKeyword), w -> w.lambda()
                .like(BlogFile::getOriginalName, searchKeyword)
                .or().like(BlogFile::getStorageType, searchKeyword)
                .or().like(BlogFile::getBucketName, searchKeyword));
        if (query.isValidOrder(BlogFile.class)) {
            qw.orderBy(true, query.getIsAsc(), StrUtil.toUnderlineCase(query.getOrderColumn()));
        } else {
            qw.lambda().orderByDesc(BlogFile::getId);
        }
        Page<BlogFile> page = fileMapper.selectPage(PageConverter.toPage(query), qw);
        List<BlogFileEntity> converted = MapstructUtils.convert(page.getRecords(), BlogFileEntity.class);
        return PageConverter.toResult(converted, page.getTotal());
    }

    @Override
    public Long save(BlogFileEntity fileEntity) {
        BlogFile converted = MapstructUtils.convert(fileEntity, BlogFile.class);
        converted.setId(null);
        int affectedRows = fileMapper.insert(converted);
        if (affectedRows != 1 || ObjUtil.isNull(converted.getId())) {
            throw new IllegalStateException("文件元数据新增失败或未返回主键");
        }
        return converted.getId();
    }

    @Override
    public BlogFileEntity selectByStorageLocation(StorageType storageType, String bucketName, String storageKey) {
        BlogFile file = fileMapper.selectOne(Wrappers.<BlogFile>lambdaQuery()
                .eq(BlogFile::getStorageType, storageType.name())
                .eq(BlogFile::getBucketName, bucketName)
                .eq(BlogFile::getStorageKey, storageKey));
        return MapstructUtils.convert(file, BlogFileEntity.class);
    }
}
