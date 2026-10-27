package com.example.domain.auth.service;

import com.example.domain.auth.model.entity.SysUserEntity;

public interface IAuthService {

    SysUserEntity handleLogin(String username, String password);
}
