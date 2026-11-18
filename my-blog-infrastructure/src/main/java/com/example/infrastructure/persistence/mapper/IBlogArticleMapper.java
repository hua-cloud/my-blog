package com.example.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.infrastructure.persistence.po.BlogArticle;
import com.example.infrastructure.persistence.po.BlogArticleDetail;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.Set;

@Mapper
public interface IBlogArticleMapper extends BaseMapper<BlogArticle> {

    BlogArticleDetail selectDetailById(@Param("id") Long id);

    int insertTagLinks(@Param("articleId") Long articleId, @Param("tagIds") Set<Long> tagIds,
                       @Param("operatorId") Long operatorId, @Param("createdAt") LocalDateTime createdAt);

    @Delete("DELETE FROM blog_article_tag WHERE article_id = #{articleId}")
    int deleteTagLinks(@Param("articleId") Long articleId);
}
