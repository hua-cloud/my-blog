package com.example.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.infrastructure.persistence.po.BlogArticle;
import com.example.infrastructure.persistence.po.BlogArticleDetail;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Mapper
public interface IBlogArticleMapper extends BaseMapper<BlogArticle> {

    BlogArticleDetail selectDetailById(@Param("id") Long id);

    BlogArticleDetail selectPublishedDetailById(@Param("id") Long id, @Param("publishedStatus") int publishedStatus);

    Page<BlogArticleDetail> selectPublishedPage(Page<BlogArticleDetail> page,
                                                @Param("publishedStatus") int publishedStatus,
                                                @Param("searchKeyword") String searchKeyword,
                                                @Param("categoryId") Long categoryId, @Param("tagId") Long tagId);

    List<BlogArticleDetail> selectTagsByArticleIds(@Param("articleIds") List<Long> articleIds);

    int insertTagLinks(@Param("articleId") Long articleId, @Param("tagIds") Set<Long> tagIds,
                       @Param("operatorId") Long operatorId, @Param("createdAt") LocalDateTime createdAt);

    @Delete("DELETE FROM blog_article_tag WHERE article_id = #{articleId}")
    int deleteTagLinks(@Param("articleId") Long articleId);
}
