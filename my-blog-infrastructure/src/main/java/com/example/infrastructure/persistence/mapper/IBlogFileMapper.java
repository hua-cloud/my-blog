package com.example.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.infrastructure.persistence.po.BlogFile;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IBlogFileMapper extends BaseMapper<BlogFile> {
}
