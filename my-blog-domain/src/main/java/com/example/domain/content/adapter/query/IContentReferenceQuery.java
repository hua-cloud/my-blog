package com.example.domain.content.adapter.query;

public interface IContentReferenceQuery {
    /** 检查全部状态的文章，不能仅查询已发布文章。 */
    boolean existsArticleUsingCategory(Long categoryId);
}
