package com.example.astaroswiki.repository;

import com.example.astaroswiki.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByParentIdIsNullOrderBySortOrder();
    List<Category> findByParentIdOrderBySortOrder(Long parentId);
    List<Category> findByParentIdIsNull();
    List<Category> findByParentId(Long parentId);
    Optional<Category> findByName(String name);
    boolean existsByName(String name);
}
