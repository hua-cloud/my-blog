package com.example.types.page;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/** 框架无关的分页参数，页码从 1 开始。 */
@Getter
public class PageQuery {

    public static final int DEFAULT_PAGE_NUM = 1;
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;

    private Integer pageNum = DEFAULT_PAGE_NUM;
    private Integer pageSize = DEFAULT_PAGE_SIZE;

    public void setPageNum(Integer pageNum) {
        int value = ObjUtil.isNull(pageNum) ? DEFAULT_PAGE_NUM : pageNum;
        checkPageNum(value);
        this.pageNum = value;
    }

    public void setPageSize(Integer pageSize) {
        int value = ObjUtil.isNull(pageSize) ? DEFAULT_PAGE_SIZE : pageSize;
        checkPageSize(value);
        this.pageSize = value;
    }

    public static void checkPageNum(int pageNum) {
        if (pageNum < 1) {
            throw new IllegalArgumentException("pageNum 必须大于等于 1");
        }
    }

    public static void checkPageSize(int pageSize) {
        if (pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("pageSize 必须在 1 到 " + MAX_PAGE_SIZE + " 之间");
        }
    }
}
