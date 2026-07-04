package com.example.astaroswiki.dto.tag;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TagCreateRequest {
    @NotBlank(message = "标签名称不能为空")
    private String name;
}
