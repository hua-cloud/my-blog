package com.example.domain.content.service.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveCategoryCommand {

    private String name;

    private String description;

    private Integer sortOrder;
}
