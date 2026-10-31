package com.example.domain.content.query.model;

import com.example.types.page.PageQuery;

public class CategoryQuery extends PageQuery {
    private String keyword;

    public String getKeyword() { return keyword; }

    public void setKeyword(String keyword) {
        String value = keyword == null ? null : keyword.strip();
        if (value != null && value.codePointCount(0, value.length()) > 64) {
            throw new IllegalArgumentException("分类查询关键字不能超过64个字符");
        }
        this.keyword = value == null || value.isEmpty() ? null : value;
    }
}
