package com.example.handler;

import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.example.infrastructure.dao.po.BasePo;
import com.example.trigger.http.security.LoginHelper;
import org.apache.ibatis.reflection.MetaObject;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AuditMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        if (ObjUtil.isNotNull(metaObject) && metaObject.getOriginalObject() instanceof BasePo basePo) {
            Integer userId = LoginHelper.getUserId();
            LocalDateTime now = LocalDateTime.now();
            basePo.setCreateBy(userId);
            basePo.setCreateTime(now);
            basePo.setUpdateBy(userId);
            basePo.setUpdateTime(now);

        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        if (ObjUtil.isNotNull(metaObject) && metaObject.getOriginalObject() instanceof BasePo basePo) {
            Integer userId = LoginHelper.getUserId();
            LocalDateTime now = LocalDateTime.now();
            basePo.setUpdateBy(userId);
            basePo.setUpdateTime(now);
        }
    }
}
