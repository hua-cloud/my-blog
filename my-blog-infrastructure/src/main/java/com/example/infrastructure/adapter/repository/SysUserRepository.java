package com.example.infrastructure.adapter.repository;

import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.domain.auth.adapter.repository.ISysUserRepository;
import com.example.domain.auth.model.entity.SysUserEntity;
import com.example.infrastructure.dao.ISysUserDao;
import com.example.infrastructure.dao.po.SysUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SysUserRepository implements ISysUserRepository {

    private final ISysUserDao sysUserDao;

    @Override
    public SysUserEntity queryActiveByUsername(String username) {
        LambdaQueryWrapper<SysUser> luw = Wrappers.lambdaQuery();
        luw.eq(SysUser::getUsername, username)
                .eq(SysUser::getStatus, 1);
        SysUser sysUser = sysUserDao.selectOne(luw);

        if (ObjUtil.isNull(sysUser)) {
            return null;
        }
        return SysUserEntity.builder()
                .id(sysUser.getId())
                .username(sysUser.getUsername())
                .password(sysUser.getPassword())
                .build();
    }
}
