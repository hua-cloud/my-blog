package com.example.domain.content.model.dto;

import com.example.domain.content.model.entity.BlogCategoryEntity;
import com.example.types.valid.AddGroup;
import com.example.types.valid.UpdateGroup;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
@AutoMapper(target = BlogCategoryEntity.class, reverseConvertGenerate = false)
public class BlogCategoryRequestDTO implements Serializable {

    @NotBlank(message = "分类名称不能为空", groups = { AddGroup.class, UpdateGroup.class })
    @Size(max = 64, message = "分类名称长度不能超过64", groups = { AddGroup.class, UpdateGroup.class })
    private String name;

    @Size(max = 255, message = "分类描述长度不能超过255", groups = { AddGroup.class, UpdateGroup.class })
    private String description;

    @Min(value = 0, message = "排序值不能小于0", groups = { AddGroup.class, UpdateGroup.class })
    private Integer sortOrder;

    private String searchKeyword;
}
