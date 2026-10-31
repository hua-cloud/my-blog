package com.example.domain.content.model.dto;

import com.example.domain.content.model.entity.BlogCategoryEntity;
import com.example.types.valid.AddGroup;
import com.example.types.valid.UpdateGroup;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
@AutoMapper(target = BlogCategoryEntity.class, reverseConvertGenerate = false)
public class BlogCategoryRequestDTO implements Serializable {

    @NotBlank(message = "分类名称不能为空", groups = {AddGroup.class, UpdateGroup.class})
    private String name;

    private String description;

    private Integer sortOrder;

    private String searchKeyword;
}
