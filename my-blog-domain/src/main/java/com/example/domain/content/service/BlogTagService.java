package com.example.domain.content.service;

import cn.hutool.core.util.ObjUtil;
import com.example.domain.content.adapter.repository.IBlogTagRepository;
import com.example.domain.content.model.dto.BlogTagResponseDTO;
import com.example.domain.content.model.entity.BlogTagEntity;
import com.example.types.core.AppException;
import com.example.types.core.PageQuery;
import com.example.types.core.PageResult;
import com.example.types.utils.MapstructUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.example.types.enums.ResultStatus.TAG_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class BlogTagService implements IBlogTagService {

    private final IBlogTagRepository tagRepository;

    @Override
    public BlogTagEntity detail(Long id) {
        BlogTagEntity entity = tagRepository.selectById(id);
        if (ObjUtil.isNull(entity)) {
            throw new AppException(TAG_NOT_FOUND);
        }
        return entity;
    }

    @Override
    public PageResult<BlogTagResponseDTO> page(String searchKeyword, PageQuery pageQuery) {
        PageResult<BlogTagEntity> pageResult = tagRepository.page(searchKeyword, pageQuery);
        List<BlogTagResponseDTO> converted = MapstructUtils.convert(pageResult.getRecords(), BlogTagResponseDTO.class);
        return PageResult.of(converted, pageResult.getTotal());
    }

    @Override
    public boolean create(BlogTagEntity tagEntity) {
        return tagRepository.save(tagEntity) > 0;
    }

    @Override
    public boolean update(BlogTagEntity tagEntity) {
        return tagRepository.update(tagEntity) > 0;
    }

    @Override
    public boolean delete(long id) {
        return tagRepository.delete(id) > 0;
    }
}
