package com.example.astaroswiki.dto.message;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageDTO {
    private Long id;
    private String title;
    private String content;
    private String opinion;
    private Boolean isRead;
    private Long pageId;
    private String pageSlug;
    private LocalDateTime createdAt;
}
