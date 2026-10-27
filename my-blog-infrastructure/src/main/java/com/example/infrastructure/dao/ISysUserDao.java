package com.example.infrastructure.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.infrastructure.dao.po.SysUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ISysUserDao extends BaseMapper<SysUser> {
}
