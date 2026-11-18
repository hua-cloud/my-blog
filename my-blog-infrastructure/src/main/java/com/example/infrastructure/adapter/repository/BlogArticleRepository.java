package com.example.infrastructure.adapter.repository;

import cn.hutool.core.collection.CollUtil;
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
import com.example.infrastructure.persistence.po.BlogArticleDetail;
import com.example.types.core.AppException;
import com.example.types.core.PageQuery;
import com.example.types.core.PageResult;
import com.example.types.utils.MapstructUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static com.example.types.enums.ResultStatus.TAG_NOT_FOUND;

@Repository
@RequiredArgsConstructor
public class BlogArticleRepository implements IBlogArticleRepository {

    private final IBlogArticleMapper articleMapper;

    @Override
    public BlogArticleEntity selectById(Long id) {
        BlogArticleDetail detail = articleMapper.selectDetailById(id);
        return MapstructUtils.convert(detail, BlogArticleEntity.class);
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
    @Transactional
    public int save(BlogArticleEntity article) {
        BlogArticle row = MapstructUtils.convert(article, BlogArticle.class);
        int inserted = articleMapper.insert(row);
        if (inserted > 0) {
            insertTagLinks(row.getId(), article.getTagIds(), row.getCreateBy(), row.getCreateTime());
        }
        return inserted;
    }

    @Override
    @Transactional
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
        BlogArticle row = new BlogArticle();
        int updated = articleMapper.update(row, luw);
        Set<Long> tagIds = article.getTagIds();
        if (updated > 0 && ObjUtil.isNotNull(tagIds)) {
            articleMapper.deleteTagLinks(article.getId());
            insertTagLinks(article.getId(), tagIds, row.getUpdateBy(), row.getUpdateTime());
        }
        return updated;
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

    private void insertTagLinks(Long articleId, Set<Long> tagIds, Long operatorId, LocalDateTime createdAt) {
        if (CollUtil.isEmpty(tagIds)) {
            return;
        }
        int inserted = articleMapper.insertTagLinks(articleId, tagIds, operatorId, createdAt);
        if (inserted != tagIds.size()) {
            throw new AppException(TAG_NOT_FOUND);
        }
    }
}
