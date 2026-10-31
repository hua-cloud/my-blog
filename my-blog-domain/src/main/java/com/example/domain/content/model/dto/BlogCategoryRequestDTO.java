package com.example.domain.content.model.dto;

import com.example.domain.content.model.entity.BlogCategoryEntity;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
@AutoMapper(target = BlogCategoryEntity.class, reverseConvertGenerate = false)
public class BlogCategoryRequestDTO implements Serializable {

    @NotBlank(message = "分类名称不能为空")
    private String name;

    private String description;

    private Integer sortOrder;

    private String searchKeyword;
}
