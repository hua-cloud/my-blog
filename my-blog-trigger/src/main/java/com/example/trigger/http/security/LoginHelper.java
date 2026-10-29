package com.example.trigger.http.security;

import cn.dev33.satoken.stp.StpUtil;

public class LoginHelper {

    /**
     * 获取当前登录用户 ID
     */
    public static Long getUserId() {
        return StpUtil.getLoginIdAsLong();
    }
}
