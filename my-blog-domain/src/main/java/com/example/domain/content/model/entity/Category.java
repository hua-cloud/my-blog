package com.example.domain.content.model.entity;

/** 分类是独立的小聚合，不持有文章集合或持久化框架类型。 */
public final class Category {
    private final Long id;
    private String name;
    private String description;
    private int sortOrder;

    private Category(Long id, String name, String description, Integer sortOrder) {
        this.id = id;
        change(name, description, sortOrder);
    }

    public static Category create(String name, String description, Integer sortOrder) {
        return new Category(null, name, description, sortOrder);
    }

    public static Category restore(Long id, String name, String description, Integer sortOrder) {
        if (id == null || id <= 0) throw new IllegalArgumentException("分类ID必须大于0");
        return new Category(id, name, description, sortOrder);
    }

    public void change(String name, String description, Integer sortOrder) {
        String normalized = name == null ? "" : name.strip();
        if (normalized.isBlank() || normalized.codePointCount(0, normalized.length()) > 64) {
            throw new IllegalArgumentException("分类名称不能为空且不能超过64个字符");
        }
        if (description != null && description.codePointCount(0, description.length()) > 255) {
            throw new IllegalArgumentException("分类描述不能超过255个字符");
        }
        this.name = normalized;
        this.description = description;
        this.sortOrder = sortOrder == null ? 0 : sortOrder;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getSortOrder() { return sortOrder; }
}
