package com.example.astaroswiki.controller;

import com.example.astaroswiki.common.Result;
import com.example.astaroswiki.dto.tag.TagCreateRequest;
import com.example.astaroswiki.entity.Tag;
import com.example.astaroswiki.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@io.swagger.v3.oas.annotations.tags.Tag(name = "标签管理", description = "标签相关接口")
@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @Operation(summary = "获取所有标签")
    @GetMapping
    public Result<List<Tag>> getAllTags() {
        List<Tag> tags = tagService.getAllTags();
        return Result.success(tags);
    }

    @Operation(summary = "创建标签")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Tag> createTag(@Valid @RequestBody TagCreateRequest request) {
        Tag tag = tagService.createTag(request.getName());
        return Result.success(tag);
    }

    @Operation(summary = "删除标签")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return Result.success();
    }

    @Operation(summary = "合并标签")
    @PostMapping("/merge")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> mergeTags(@RequestParam Long sourceId, @RequestParam Long targetId) {
        tagService.mergeTags(sourceId, targetId);
        return Result.success();
    }
}
