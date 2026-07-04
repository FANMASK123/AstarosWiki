package com.example.astaroswiki.dto.page;

import lombok.Data;

import java.util.Set;

@Data
public class PageUpdateRequest {
    private String title;

    private String content;

    private Long categoryId;

    private Set<String> tags;

    private Boolean publish;
}
