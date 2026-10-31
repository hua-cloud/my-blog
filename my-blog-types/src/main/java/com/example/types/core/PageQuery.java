package com.example.types.core;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/** 框架无关的分页参数，页码从 1 开始。 */
@Getter
@Setter
public class PageQuery {

    private Integer pageNum;

    private Integer pageSize;

    private String orderColumn;

    private String orderType;

    public boolean isValidOrder(Class<?> clazz) {
        Set<String> fieldNames = Arrays.stream(clazz.getFields())
                .map(Field::getName)
                .collect(Collectors.toSet());
        return StrUtil.isNotBlank(orderColumn) && StrUtil.isNotBlank(orderType) && fieldNames.contains(orderColumn);
    }

    public boolean getIsAsc() {
        return "asc".equals(orderColumn);
    }
}


