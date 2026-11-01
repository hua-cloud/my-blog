package com.example.domain.content.model.entity;

import com.example.domain.content.model.dto.BlogTagResponseDTO;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@AutoMapper(target = BlogTagResponseDTO.class, reverseConvertGenerate = false)
public class BlogTagEntity {

    private Long id;

    private String name;

    private String color;
}
