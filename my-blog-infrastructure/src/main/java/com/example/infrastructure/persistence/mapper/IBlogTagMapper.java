package com.example.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.infrastructure.persistence.po.BlogTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IBlogTagMapper extends BaseMapper<BlogTag> {

    List<BlogTag> selectWithPublishedArticles(@Param("publishedStatus") int publishedStatus);
}
