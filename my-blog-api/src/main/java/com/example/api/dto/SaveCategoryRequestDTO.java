package com.example.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** PUT 为全量编辑：description 为空时清除，sortOrder 为空时使用 0。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveCategoryRequestDTO {

    @NotBlank(message = "分类名称不能为空")
    private String name;

    private String description;

    private Integer sortOrder;
}
