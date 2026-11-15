package com.example.domain.content.service;

import cn.hutool.core.util.ObjUtil;
import com.example.domain.content.adapter.repository.IBlogArticleRepository;
import com.example.domain.content.adapter.repository.IBlogCategoryRepository;
import com.example.domain.content.model.dto.BlogArticleResponseDTO;
import com.example.domain.content.model.entity.BlogArticleEntity;
import com.example.domain.content.model.valobj.ArticleStatus;
import com.example.domain.file.adapter.repository.IBlogFileRepository;
import com.example.types.core.AppException;
import com.example.types.core.PageQuery;
import com.example.types.core.PageResult;
import com.example.types.utils.MapstructUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.types.enums.ResultStatus.*;

@Service
@RequiredArgsConstructor
public class BlogArticleService implements IBlogArticleService {

    private final IBlogArticleRepository articleRepository;

    private final IBlogCategoryRepository categoryRepository;

    private final IBlogFileRepository fileRepository;

    @Override
    public BlogArticleEntity detail(Long id) {
        BlogArticleEntity article = articleRepository.selectById(id);
        if (article == null) {
            throw new AppException(ARTICLE_NOT_FOUND);
        }
        return article;
    }

    @Override
    public PageResult<BlogArticleResponseDTO> page(String searchKeyword, Integer status, PageQuery pageQuery) {
        PageResult<BlogArticleEntity> page = articleRepository.page(searchKeyword, status, pageQuery);
        List<BlogArticleResponseDTO> records = MapstructUtils.convert(page.getRecords(), BlogArticleResponseDTO.class);
        return PageResult.of(records, page.getTotal());
    }

    @Override
    public boolean create(BlogArticleEntity article) {
        if (article.getStatus() == null) {
            article.setStatus(ArticleStatus.DRAFT.getCode());
        }
        prepareWrite(article);
        return articleRepository.save(article) > 0;
    }

    @Override
    public boolean update(BlogArticleEntity article) {
        prepareWrite(article);
        return articleRepository.update(article) > 0;
    }

    @Override
    public boolean delete(Long id) {
        return articleRepository.delete(id) > 0;
    }

    private void prepareWrite(BlogArticleEntity article) {
        if (ObjUtil.isNotNull(article.getCategoryId()) && !categoryRepository.existsById(article.getCategoryId())) {
            throw new AppException(CATEGORY_NOT_FOUND);
        }
        if (ObjUtil.isNotNull(article.getCoverFileId()) && !fileRepository.existsFileById(article.getCoverFileId())) {
            throw new AppException(ARTICLE_COVER_NOT_FOUND);
        }
        if (ArticleStatus.PUBLISHED.getCode() == article.getStatus()) {
            article.setPublishedAt(LocalDateTime.now());
        }
    }
}
