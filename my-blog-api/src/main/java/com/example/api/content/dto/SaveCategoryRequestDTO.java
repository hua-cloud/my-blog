package com.example.api.content.dto;

import jakarta.validation.constraints.NotBlank;

/** PUT 为全量编辑：description 为空时清除，sortOrder 为空时使用 0。 */
public record SaveCategoryRequestDTO(
        @NotBlank(message = "分类名称不能为空") String name,
        String description,
        Integer sortOrder) {}
