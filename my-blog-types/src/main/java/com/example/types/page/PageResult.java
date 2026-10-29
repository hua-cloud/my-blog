package com.example.types.page;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/** 框架无关的分页结果；记录列表不可修改，空结果使用空列表。 */
@Getter
public final class PageResult<T> {

    private final List<T> records;

    private final long total;

    private final int pageNum;

    private final int pageSize;

    private PageResult(List<? extends T> records, long total, int pageNum, int pageSize) {
        this.records = records == null ? List.of() : List.copyOf(records);
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }

    public static <T> PageResult<T> of(List<? extends T> records, long total, int pageNum, int pageSize) {
        return new PageResult<>(records, total, pageNum, pageSize);
    }

    /** 在模型、查询投影及 DTO 之间转换记录，保留分页信息。 */
    public <R> PageResult<R> map(Function<? super T, ? extends R> mapper) {
        Objects.requireNonNull(mapper, "mapper 不能为空");
        List<R> mapped = new ArrayList<>(records.size());
        for (T record : records) {
            mapped.add(mapper.apply(record));
        }
        return PageResult.of(mapped, total, pageNum, pageSize);
    }
}
