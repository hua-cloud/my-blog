package com.example.domain.content.query.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryView {

    private Long id;

    private String name;

    private String description;

    private int sortOrder;
}
