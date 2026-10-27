package com.example.handler;

import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.example.types.core.BaseEntity;
import com.example.types.utils.LoginHelper;
import org.apache.ibatis.reflection.MetaObject;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AuditMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        if (ObjUtil.isNotNull(metaObject) && metaObject.getOriginalObject() instanceof BaseEntity baseEntity) {
            Integer userId = LoginHelper.getUserId();
            LocalDateTime now = LocalDateTime.now();
            baseEntity.setCreateBy(userId);
            baseEntity.setCreateTime(now);
            baseEntity.setUpdateBy(userId);
            baseEntity.setUpdateTime(now);

        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        if (ObjUtil.isNotNull(metaObject) && metaObject.getOriginalObject() instanceof BaseEntity baseEntity) {
            Integer userId = LoginHelper.getUserId();
            LocalDateTime now = LocalDateTime.now();
            baseEntity.setUpdateBy(userId);
            baseEntity.setUpdateTime(now);
        }
    }
}
