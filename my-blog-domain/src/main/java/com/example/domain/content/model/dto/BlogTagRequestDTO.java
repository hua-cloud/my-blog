package com.example.domain.content.model.dto;

import com.example.domain.content.model.entity.BlogTagEntity;
import com.example.types.valid.AddGroup;
import com.example.types.valid.UpdateGroup;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
@AutoMapper(target = BlogTagEntity.class, reverseConvertGenerate = false)
public class BlogTagRequestDTO implements Serializable {

    @NotBlank(message = "标签名称不能为空", groups = { AddGroup.class, UpdateGroup.class })
    @Size(max = 64, message = "标签名称长度不能超过64", groups = { AddGroup.class, UpdateGroup.class })
    private String name;

    private String searchKeyword;
}
