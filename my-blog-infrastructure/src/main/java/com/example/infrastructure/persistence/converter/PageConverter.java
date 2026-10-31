package com.example.infrastructure.persistence.converter;

import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.types.core.PageQuery;
import com.example.types.core.PageResult;

import java.util.List;

import static com.example.types.constant.Constants.DEFAULT_PAGE_NUM;
import static com.example.types.constant.Constants.DEFAULT_PAGE_SIZE;

/** MyBatis-Plus 分页类型仅在基础设施内部使用。 */
public final class PageConverter {

    public static <T> Page<T> toPage(PageQuery query) {
        Integer actualPageNum = ObjUtil.defaultIfNull(query.getPageNum(), DEFAULT_PAGE_NUM);
        Integer actualPageSize = ObjUtil.defaultIfNull(query.getPageSize(), DEFAULT_PAGE_SIZE);
        return new Page<>(actualPageNum, actualPageSize);
    }

    public static <T> PageResult<T> toResult(List<T> records, long total) {
        return PageResult.of(records, total);
    }
}
