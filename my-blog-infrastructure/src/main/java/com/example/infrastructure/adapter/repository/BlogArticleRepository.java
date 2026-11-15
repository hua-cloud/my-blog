package com.example.infrastructure.adapter.repository;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.domain.content.adapter.repository.IBlogArticleRepository;
import com.example.domain.content.model.entity.BlogArticleEntity;
import com.example.infrastructure.persistence.converter.PageConverter;
import com.example.infrastructure.persistence.mapper.IBlogArticleMapper;
import com.example.infrastructure.persistence.po.BlogArticle;
import com.example.types.core.PageQuery;
import com.example.types.core.PageResult;
import com.example.types.utils.MapstructUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class BlogArticleRepository implements IBlogArticleRepository {

    private final IBlogArticleMapper articleMapper;

    @Override
    public BlogArticleEntity selectById(Long id) {
        return MapstructUtils.convert(articleMapper.selectById(id), BlogArticleEntity.class);
    }

    @Override
    public PageResult<BlogArticleEntity> page(String searchKeyword, Integer status, PageQuery query) {
        QueryWrapper<BlogArticle> qw = Wrappers.query();
        qw.lambda().select(BlogArticle.class, field -> !"contentMarkdown".equals(field.getProperty()))
                .like(StrUtil.isNotBlank(searchKeyword), BlogArticle::getTitle, searchKeyword)
                .eq(ObjUtil.isNotNull(status), BlogArticle::getStatus, status);
        if (query.isValidOrder(BlogArticle.class)) {
            qw.orderBy(true, query.getIsAsc(), StrUtil.toUnderlineCase(query.getOrderColumn()));
        } else {
            qw.lambda().orderByAsc(BlogArticle::getId);
        }
        Page<BlogArticle> page = articleMapper.selectPage(PageConverter.toPage(query), qw);
        List<BlogArticleEntity> records = MapstructUtils.convert(page.getRecords(), BlogArticleEntity.class);
        return PageConverter.toResult(records, page.getTotal());
    }

    @Override
    public int save(BlogArticleEntity article) {
        return articleMapper.insert(MapstructUtils.convert(article, BlogArticle.class));
    }

    @Override
    public int update(BlogArticleEntity article) {
        LambdaUpdateWrapper<BlogArticle> luw = Wrappers.lambdaUpdate();
        luw.set(StrUtil.isNotBlank(article.getTitle()), BlogArticle::getTitle, article.getTitle())
                .set(ObjUtil.isNotNull(article.getSummary()), BlogArticle::getSummary, article.getSummary())
                .set(StrUtil.isNotBlank(article.getContentMarkdown()), BlogArticle::getContentMarkdown, article.getContentMarkdown())
                .set(BlogArticle::getCategoryId, article.getCategoryId())
                .set(BlogArticle::getCoverFileId, article.getCoverFileId())
                .set(ObjUtil.isNotNull(article.getStatus()), BlogArticle::getStatus, article.getStatus())
                .eq(BlogArticle::getId, article.getId());
        if (ObjUtil.isNotNull(article.getPublishedAt())) {
            luw.setSql("published_at = COALESCE(published_at, {0})", article.getPublishedAt());
        }
        return articleMapper.update(new BlogArticle(), luw);
    }

    @Override
    @Transactional
    public int delete(Long id) {
        int deleted = articleMapper.deleteById(id);
        if (deleted > 0) {
            articleMapper.deleteTagLinks(id);
        }
        return deleted;
    }
}
