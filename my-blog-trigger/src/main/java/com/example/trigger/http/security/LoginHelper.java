package com.example.trigger.http.security;

import cn.dev33.satoken.stp.StpUtil;

public class LoginHelper {

    /**
     * 获取当前登录用户 ID
     */
    public static Integer getUserId() {
        return StpUtil.getLoginIdAsInt();
    }
}
