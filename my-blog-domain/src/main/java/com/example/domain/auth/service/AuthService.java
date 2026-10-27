package com.example.domain.auth.service;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.example.domain.auth.adapter.repository.ISysUserRepository;
import com.example.domain.auth.model.entity.SysUserEntity;
import com.example.types.core.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.example.types.enums.ResultStatus.BAD_CREDENTIALS;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService{

    private final ISysUserRepository sysUserRepository;

    @Override
    public SysUserEntity handleLogin(String username, String password) {
        // 1. 根据用户名查询用户
        SysUserEntity entity = sysUserRepository.queryActiveByUsername(username);
        if (ObjUtil.isNull(entity)) {
            throw new AppException(BAD_CREDENTIALS);
        }
        // 2. 校验密码
        if (!BCrypt.checkpw(password, entity.getPassword())) {
            throw new AppException(BAD_CREDENTIALS);
        }
        return entity;
    }
}
