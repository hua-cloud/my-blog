package com.example.domain.content.service;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.example.domain.content.adapter.repository.IBlogArticleRepository;
import com.example.domain.content.adapter.repository.IBlogCategoryRepository;
import com.example.domain.content.adapter.repository.IBlogTagRepository;
import com.example.domain.content.model.dto.BlogCategoryResponseDTO;
import com.example.domain.content.model.dto.BlogTagResponseDTO;
import com.example.domain.content.model.dto.PublicArticleDetailResponseDTO;
import com.example.domain.content.model.dto.PublicArticleSummaryResponseDTO;
import com.example.domain.content.model.entity.BlogArticleEntity;
import com.example.types.core.AppException;
import com.example.types.core.PageQuery;
import com.example.types.core.PageResult;
import com.example.types.utils.MapstructUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.example.types.enums.ResultStatus.ARTICLE_NOT_FOUND;

@Service
public class PublicArticleQueryService implements IPublicArticleQueryService {

    private final IBlogArticleRepository articleRepository;

    private final IBlogCategoryRepository categoryRepository;

    private final IBlogTagRepository tagRepository;

    private final String coverBaseUrl;

    public PublicArticleQueryService(IBlogArticleRepository articleRepository, IBlogCategoryRepository categoryRepository,
                                     IBlogTagRepository tagRepository, @Value("${file.storage.r2.public-base-url}") String coverBaseUrl) {
        this.articleRepository = articleRepository;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
        this.coverBaseUrl = StrUtil.addSuffixIfNot(coverBaseUrl, "/");
    }

    @Override
    public PageResult<PublicArticleSummaryResponseDTO> page(String searchKeyword, Long categoryId, Long tagId, PageQuery pageQuery) {
        PageResult<BlogArticleEntity> page = articleRepository.pagePublished(searchKeyword, categoryId, tagId, pageQuery);
        List<PublicArticleSummaryResponseDTO> records = page.getRecords().stream()
                .map(article -> toResponse(article, PublicArticleSummaryResponseDTO.class))
                .toList();
        return PageResult.of(records, page.getTotal());
    }

    @Override
    public PublicArticleDetailResponseDTO detail(Long id) {
        BlogArticleEntity article = articleRepository.findPublishedById(id);
        if (ObjUtil.isNull(article)) {
            throw new AppException(ARTICLE_NOT_FOUND);
        }
        return toResponse(article, PublicArticleDetailResponseDTO.class);
    }

    @Override
    public List<BlogCategoryResponseDTO> listCategories() {
        return MapstructUtils.convert(categoryRepository.listWithPublishedArticles(), BlogCategoryResponseDTO.class);
    }

    @Override
    public List<BlogTagResponseDTO> listTags() {
        return MapstructUtils.convert(tagRepository.listWithPublishedArticles(), BlogTagResponseDTO.class);
    }

    private <T extends PublicArticleSummaryResponseDTO> T toResponse(BlogArticleEntity article, Class<T> type) {
        T target = MapstructUtils.convert(article, type);
        if (StrUtil.isNotBlank(article.getCoverStorageKey())) {
            target.setCoverUrl(coverBaseUrl + article.getCoverStorageKey());
        }
        return target;
    }
}
