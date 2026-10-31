package com.example.domain.content.model.entity;

import com.example.domain.content.model.dto.BlogCategoryResponseDTO;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@AutoMapper(target = BlogCategoryResponseDTO.class, reverseConvertGenerate = false)
public class BlogCategoryEntity {

    private Long id;

    private String name;

    private String description;

    private int sortOrder;
}
