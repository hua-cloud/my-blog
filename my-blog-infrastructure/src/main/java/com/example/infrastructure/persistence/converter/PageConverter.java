package com.example.infrastructure.persistence.converter;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.types.page.PageQuery;
import com.example.types.page.PageResult;

import java.util.Objects;
import java.util.function.Function;

/** MyBatis-Plus 分页类型仅在基础设施内部使用。 */
public final class PageConverter {

    public static <T> Page<T> toPage(PageQuery query) {
        Objects.requireNonNull(query, "query 不能为空");
        return new Page<>(query.getPageNum(), query.getPageSize(), true);
    }

    public static <T> PageResult<T> toResult(IPage<T> page) {
        Objects.requireNonNull(page, "page 不能为空");
        return PageResult.of(page.getRecords(), page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    public static <S, T> PageResult<T> toResult(IPage<S> page, Function<? super S, ? extends T> mapper) {
        return toResult(page).map(mapper);
    }
}
