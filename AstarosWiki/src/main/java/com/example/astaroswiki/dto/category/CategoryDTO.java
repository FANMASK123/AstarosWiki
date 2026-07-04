package com.example.astaroswiki.dto.category;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CategoryDTO {
    private Long id;
    private String name;
    private String description;
    private Long parentId;
    private Integer sortOrder;
    private List<CategoryDTO> children = new ArrayList<>();
}
