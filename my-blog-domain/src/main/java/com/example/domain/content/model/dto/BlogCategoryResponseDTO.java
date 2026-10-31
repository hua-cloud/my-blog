package com.example.domain.content.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogCategoryResponseDTO implements Serializable {

    private Long id;

    private String name;

    private String description;

    private Integer sortOrder;
}
