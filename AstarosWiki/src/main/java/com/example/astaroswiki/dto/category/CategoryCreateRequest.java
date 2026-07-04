package com.example.astaroswiki.dto.category;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryCreateRequest {
    @NotBlank(message = "分类名称不能为空")
    private String name;

    private String description;

    private Long parentId;

    private Integer sortOrder = 0;
}
