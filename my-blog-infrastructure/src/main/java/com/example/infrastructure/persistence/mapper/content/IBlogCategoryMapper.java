package com.example.infrastructure.persistence.mapper.content;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.infrastructure.persistence.po.CategoryPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface IBlogCategoryMapper extends BaseMapper<CategoryPo> {
    @Select("SELECT id, name, description, sort_order, create_by, create_time, update_by, update_time "
            + "FROM blog_category WHERE id = #{id} FOR UPDATE")
    CategoryPo selectByIdForUpdate(@Param("id") Long id);
}
