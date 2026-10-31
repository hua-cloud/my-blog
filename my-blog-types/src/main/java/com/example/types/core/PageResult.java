package com.example.types.core;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/** 框架无关的分页结果；记录列表不可修改，空结果使用空列表。 */
@Getter
@Setter
public final class PageResult<T> {

    private final long total;

    private List<T> records;


    private PageResult(List<T> records, long total) {
        this.records = records;
        this.total = total;
    }

    public static <T> PageResult<T> of(List<T> records, long total) {
        return new PageResult<>(records, total);
    }
}
