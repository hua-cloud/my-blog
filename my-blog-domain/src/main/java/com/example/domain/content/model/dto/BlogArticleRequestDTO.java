package com.example.domain.content.model.dto;

import com.example.domain.content.model.entity.BlogArticleEntity;
import com.example.types.valid.AddGroup;
import com.example.types.valid.UpdateGroup;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.Set;

@Data
@AutoMapper(target = BlogArticleEntity.class, reverseConvertGenerate = false)
public class BlogArticleRequestDTO {

    @NotBlank(message = "文章标题不能为空", groups = { AddGroup.class, UpdateGroup.class })
    @Size(max = 200, message = "文章标题长度不能超过200", groups = { AddGroup.class, UpdateGroup.class })
    private String title;

    @NotNull(message = "文章摘要不能为 null", groups = { AddGroup.class, UpdateGroup.class })
    @Size(max = 500, message = "文章摘要长度不能超过500", groups = { AddGroup.class, UpdateGroup.class })
    private String summary;

    @NotBlank(message = "文章正文不能为空", groups = { AddGroup.class, UpdateGroup.class })
    private String contentMarkdown;

    private Long categoryId;

    private Long coverFileId;

    /** 更新时 null 表示保留标签，空列表表示清空。 */
    @NotNull(message = "标签ID不能为 null", groups = { AddGroup.class, UpdateGroup.class })
    private Set<Long> tagIds;

    @NotNull(message = "文章状态不能为 null", groups = { AddGroup.class, UpdateGroup.class })
    @Min(value = 0, message = "文章状态无效", groups = { AddGroup.class, UpdateGroup.class })
    @Max(value = 2, message = "文章状态无效", groups = { AddGroup.class, UpdateGroup.class })
    private Integer status;

    private String searchKeyword;
}
