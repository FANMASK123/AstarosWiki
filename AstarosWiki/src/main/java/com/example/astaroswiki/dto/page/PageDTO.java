package com.example.astaroswiki.dto.page;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class PageDTO {
    private Long id;
    private String title;
    private String content; // 已发布内容
    private String draftContent; // 草稿内容（仅作者可见）
    private String slug;
    private Long categoryId;
    private String categoryName;
    private Long authorId;
    private String authorName;
    private String status; // 主状态：PUBLISHED/SAVED/DELETED/LOCKED
    private String draftStatus; // 草稿状态：NULL/SAVED/DRAFT
    private Integer version;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime draftUpdatedAt;
    private Set<String> tags;
    private String rejectReason;
}
