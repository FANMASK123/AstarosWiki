package com.example.astaroswiki.controller;

import com.example.astaroswiki.common.Result;
import com.example.astaroswiki.dto.category.CategoryCreateRequest;
import com.example.astaroswiki.dto.category.CategoryDTO;
import com.example.astaroswiki.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "分类管理", description = "分类相关接口")
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "获取所有分类")
    @GetMapping
    public Result<List<CategoryDTO>> getAllCategories() {
        List<CategoryDTO> categories = categoryService.getAllCategories();
        return Result.success(categories);
    }

    @Operation(summary = "获取分类详情")
    @GetMapping("/{id}")
    public Result<CategoryDTO> getCategoryById(@PathVariable Long id) {
        CategoryDTO category = categoryService.getCategoryById(id);
        return Result.success(category);
    }

    @Operation(summary = "创建分类")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<CategoryDTO> createCategory(@Valid @RequestBody CategoryCreateRequest request) {
        CategoryDTO category = categoryService.createCategory(request);
        return Result.success(category);
    }

    @Operation(summary = "更新分类")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<CategoryDTO> updateCategory(
        @PathVariable Long id,
        @Valid @RequestBody CategoryCreateRequest request
    ) {
        CategoryDTO category = categoryService.updateCategory(id, request);
        return Result.success(category);
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success();
    }
}
