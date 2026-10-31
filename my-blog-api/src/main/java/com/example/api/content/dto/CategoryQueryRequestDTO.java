package com.example.api.content.dto;

import com.example.types.page.PageQuery;

public class CategoryQueryRequestDTO extends PageQuery {
    private String keyword;

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
}
