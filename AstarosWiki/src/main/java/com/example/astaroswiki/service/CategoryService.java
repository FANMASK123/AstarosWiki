package com.example.astaroswiki.service;

import com.example.astaroswiki.dto.category.CategoryCreateRequest;
import com.example.astaroswiki.dto.category.CategoryDTO;
import com.example.astaroswiki.entity.Category;
import com.example.astaroswiki.exception.BusinessException;
import com.example.astaroswiki.repository.CategoryRepository;
import com.example.astaroswiki.repository.PageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private static final int MAX_CATEGORY_LEVEL = 3;
    private final CategoryRepository categoryRepository;
    private final PageRepository pageRepository;
    private final Collator chineseCollator = Collator.getInstance(Locale.CHINA);

    public List<CategoryDTO> getAllCategories() {
        List<Category> rootCategories = categoryRepository.findByParentIdIsNull();
        return buildCategoryTree(rootCategories);
    }

    public CategoryDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new BusinessException("分类不存在"));
        CategoryDTO dto = convertToDTO(category);
        List<Category> children = categoryRepository.findByParentId(id);
        if (!children.isEmpty()) {
            dto.setChildren(children.stream().map(this::convertToDTO).collect(Collectors.toList()));
        }
        return dto;
    }

    @Transactional
    public CategoryDTO createCategory(CategoryCreateRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new BusinessException("分类名称已存在");
        }

        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                .orElseThrow(() -> new BusinessException("父分类不存在"));
            int level = getCategoryLevel(parent);
            if (level >= MAX_CATEGORY_LEVEL) {
                throw new BusinessException("分类最多支持三级");
            }
        }

        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setParentId(request.getParentId());
        category.setSortOrder(request.getSortOrder());

        category = categoryRepository.save(category);
        return convertToDTO(category);
    }

    @Transactional
    public CategoryDTO updateCategory(Long id, CategoryCreateRequest request) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new BusinessException("分类不存在"));

        if (!category.getName().equals(request.getName()) && categoryRepository.existsByName(request.getName())) {
            throw new BusinessException("分类名称已存在");
        }

        if (request.getParentId() != null) {
            if (request.getParentId().equals(id)) {
                throw new BusinessException("不能将自己设置为父分类");
            }
            Category parent = categoryRepository.findById(request.getParentId())
                .orElseThrow(() -> new BusinessException("父分类不存在"));
            int level = getCategoryLevel(parent);
            if (level >= MAX_CATEGORY_LEVEL) {
                throw new BusinessException("分类最多支持三级");
            }
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setParentId(request.getParentId());
        category.setSortOrder(request.getSortOrder());

        category = categoryRepository.save(category);
        return convertToDTO(category);
    }

    @Transactional
    public void deleteCategory(Long id, Long migrateTo) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new BusinessException("分类不存在"));

        List<Category> children = categoryRepository.findByParentId(id);
        if (!children.isEmpty()) {
            throw new BusinessException("该分类下还有子分类，请先删除子分类");
        }

        // 如果有迁移目标，先迁移页面
        if (migrateTo != null) {
            Category target = categoryRepository.findById(migrateTo)
                .orElseThrow(() -> new BusinessException("目标分类不存在"));
            int count = pageRepository.migratePagesToCategory(id, migrateTo);
            // 日志：迁移了多少页面
        }

        categoryRepository.delete(category);
    }

    private int getCategoryLevel(Category category) {
        int level = 1;
        Long parentId = category.getParentId();
        while (parentId != null) {
            Category parent = categoryRepository.findById(parentId).orElse(null);
            if (parent == null) break;
            level++;
            parentId = parent.getParentId();
        }
        return level;
    }

    private List<CategoryDTO> buildCategoryTree(List<Category> categories) {
        return categories.stream()
            .sorted(Comparator.comparing(Category::getName, chineseCollator))
            .map(category -> {
                CategoryDTO dto = convertToDTO(category);
                List<Category> children = categoryRepository.findByParentId(category.getId());
                if (!children.isEmpty()) {
                    dto.setChildren(buildCategoryTree(children));
                }
                return dto;
            })
            .collect(Collectors.toList());
    }

    private CategoryDTO convertToDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setParentId(category.getParentId());
        dto.setSortOrder(category.getSortOrder());
        dto.setChildren(new ArrayList<>());
        return dto;
    }
}
