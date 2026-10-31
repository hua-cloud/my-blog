package com.example.infrastructure.persistence.mapper.content;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ICategoryReferenceMapper {
    @Select("SELECT EXISTS(SELECT 1 FROM blog_article WHERE category_id = #{categoryId})")
    boolean existsArticleUsingCategory(@Param("categoryId") Long categoryId);
}
