package com.example.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.infrastructure.persistence.po.BlogArticle;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface IBlogArticleMapper extends BaseMapper<BlogArticle> {

    @Delete("DELETE FROM blog_article_tag WHERE article_id = #{articleId}")
    int deleteTagLinks(@Param("articleId") Long articleId);
}
