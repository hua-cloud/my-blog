package com.example.domain.content.service;

import cn.hutool.core.util.ObjUtil;
import com.example.domain.content.adapter.repository.ICategoryRepository;
import com.example.domain.content.model.dto.BlogCategoryResponseDTO;
import com.example.domain.content.model.entity.BlogCategoryEntity;
import com.example.types.core.AppException;
import com.example.types.core.PageQuery;
import com.example.types.core.PageResult;
import com.example.types.utils.MapstructUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.example.types.enums.ResultStatus.CATEGORY_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class BlogCategoryService implements IBlogCategoryService {

    private final ICategoryRepository categoryRepository;

    @Override
    public BlogCategoryEntity detail(Long id) {
        BlogCategoryEntity entity = categoryRepository.selectById(id);
        if (ObjUtil.isNull(entity)) {
            throw new AppException(CATEGORY_NOT_FOUND);
        }
        return entity;
    }

    @Override
    public PageResult<BlogCategoryResponseDTO> page(String searchKeyword, PageQuery pageQuery) {
        PageResult<BlogCategoryEntity> pageResult = categoryRepository.page(searchKeyword, pageQuery);
        List<BlogCategoryResponseDTO> converted = MapstructUtils.convert(pageResult.getRecords(), BlogCategoryResponseDTO.class);
        return PageResult.of(converted, pageResult.getTotal());
    }

    @Override
    public boolean create(BlogCategoryEntity categoryEntity) {
        return categoryRepository.save(categoryEntity) > 0;
    }

    @Override
    public boolean update(BlogCategoryEntity categoryEntity) {
        return categoryRepository.update(categoryEntity) > 0;
    }

    @Override
    public boolean delete(long id) {
        return categoryRepository.delete(id) > 0;
    }
}
