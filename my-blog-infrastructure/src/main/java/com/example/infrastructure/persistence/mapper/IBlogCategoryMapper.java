package com.example.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.infrastructure.persistence.po.BlogCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IBlogCategoryMapper extends BaseMapper<BlogCategory> {

    List<BlogCategory> selectWithPublishedArticles(@Param("publishedStatus") int publishedStatus);
}
