package com.example.astaroswiki.common;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
public class PageResult<T> {
    private List<T> content;
    private Long total;
    private Integer page;
    private Integer size;
    private Integer totalPages;

    public PageResult() {
    }

    public PageResult(List<T> content, Long total, Integer page, Integer size, Integer totalPages) {
        this.content = content;
        this.total = total;
        this.page = page;
        this.size = size;
        this.totalPages = totalPages;
    }

    public static <T> PageResult<T> of(Page<T> page) {
        return new PageResult<>(
            page.getContent(),
            page.getTotalElements(),
            page.getNumber(),
            page.getSize(),
            page.getTotalPages()
        );
    }
}
