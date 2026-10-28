package com.example.infrastructure.persistence.audit;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.example.infrastructure.persistence.po.BasePo;
import org.apache.ibatis.reflection.MetaObject;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static com.example.types.constant.Constants.DEFAULT_OPERATOR_ID;

@Component
public class AuditMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        if (ObjUtil.isNotNull(metaObject) && metaObject.getOriginalObject() instanceof BasePo basePo) {
            LocalDateTime now = LocalDateTime.now();
            Integer operatorId = getCurrentOperatorId();
            basePo.setCreateBy(operatorId);
            basePo.setCreateTime(now);
            basePo.setUpdateBy(operatorId);
            basePo.setUpdateTime(now);

        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        if (ObjUtil.isNotNull(metaObject) && metaObject.getOriginalObject() instanceof BasePo basePo) {
            LocalDateTime now = LocalDateTime.now();
            basePo.setUpdateBy(getCurrentOperatorId());
            basePo.setUpdateTime(now);
        }
    }

    private Integer getCurrentOperatorId() {
        return StpUtil.isLogin() ? StpUtil.getLoginIdAsInt() : DEFAULT_OPERATOR_ID;
    }
}
