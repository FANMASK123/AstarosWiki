package com.example.astaroswiki.dto.page;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class PageCreateRequest {
    @NotBlank(message = "标题不能为空")
    private String title;

    private String content;

    @NotBlank(message = "Slug不能为空")
    private String slug;

    private Long categoryId;

    private List<String> tags;

    private Boolean publish;
}
