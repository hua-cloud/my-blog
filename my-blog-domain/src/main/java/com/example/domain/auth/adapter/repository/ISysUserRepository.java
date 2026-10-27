package com.example.domain.auth.adapter.repository;

import com.example.domain.auth.model.entity.SysUserEntity;

public interface ISysUserRepository {

    SysUserEntity queryActiveByUsername(String username);
}
