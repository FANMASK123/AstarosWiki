package com.example.astaroswiki.repository;

import com.example.astaroswiki.entity.WikiPage;
import com.example.astaroswiki.enums.PageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PageRepository extends JpaRepository<WikiPage, Long> {
    Optional<WikiPage> findBySlug(String slug);
    boolean existsBySlug(String slug);

    Page<WikiPage> findByStatus(PageStatus status, Pageable pageable);
    Page<WikiPage> findByCategoryId(Long categoryId, Pageable pageable);
    Page<WikiPage> findByAuthorId(Long authorId, Pageable pageable);

    @Query("SELECT p FROM WikiPage p WHERE p.status = :status ORDER BY p.updatedAt DESC")
    List<WikiPage> findTop10ByStatusOrderByUpdatedAtDesc(@Param("status") PageStatus status, Pageable pageable);

    @Query("SELECT p FROM WikiPage p JOIN p.tags t WHERE t.name = :tagName AND p.status = :status")
    Page<WikiPage> findByTagNameAndStatus(
        @Param("tagName") String tagName,
        @Param("status") PageStatus status,
        Pageable pageable);

    @Query("SELECT p FROM WikiPage p WHERE (p.title LIKE %:keyword% OR p.content LIKE %:keyword% "
        + "OR p.draftContent LIKE %:keyword%) AND p.status = :status")
    Page<WikiPage> searchByKeywordAndStatus(
        @Param("keyword") String keyword,
        @Param("status") PageStatus status,
        Pageable pageable);

    Page<WikiPage> findByAuthorIdAndStatus(Long authorId, PageStatus status, Pageable pageable);

    Page<WikiPage> findByStatusIn(List<PageStatus> statuses, Pageable pageable);

    // 查询指定分类及其所有子分类下的已发布页面
    @Query("SELECT p FROM WikiPage p WHERE p.categoryId IN :categoryIds AND p.status = :status")
    Page<WikiPage> findByCategoryIdsAndStatus(
        @Param("categoryIds") List<Long> categoryIds,
        @Param("status") PageStatus status,
        Pageable pageable);

    // 查询待审核的页面（包括新建页面和已发布页面的草稿）
    @Query("SELECT DISTINCT p FROM WikiPage p WHERE p.status = :status OR p.draftStatus = :draftStatus")
    Page<WikiPage> findPendingReview(
        @Param("status") PageStatus status,
        @Param("draftStatus") PageStatus draftStatus,
        Pageable pageable);

    // 查询保存态页面（包括新建保存和已发布页面的草稿保存）
    @Query("SELECT DISTINCT p FROM WikiPage p WHERE p.status = :status OR p.draftStatus = :draftStatus")
    Page<WikiPage> findByStatusOrDraftStatus(
        @Param("status") PageStatus status,
        @Param("draftStatus") PageStatus draftStatus,
        Pageable pageable);

    // 查询指定作者的保存态页面（包括新建保存和已发布页面的草稿保存）
    @Query("SELECT DISTINCT p FROM WikiPage p WHERE p.authorId = :authorId "
        + "AND (p.status = :status OR p.draftStatus = :draftStatus)")
    Page<WikiPage> findByAuthorIdAndStatusOrDraftStatus(
        @Param("authorId") Long authorId,
        @Param("status") PageStatus status,
        @Param("draftStatus") PageStatus draftStatus,
        Pageable pageable);

    // 原生SQL更新浏览次数和最后浏览时间，避免触发@PreUpdate和MySQL的ON UPDATE CURRENT_TIMESTAMP
    @Modifying
    @Query(value = "UPDATE pages SET view_count = view_count + 1, "
        + "last_viewed_at = NOW(), updated_at = updated_at WHERE id = :id",
        nativeQuery = true)
    void updateViewCount(@Param("id") Long id);

    // 批量迁移页面到另一个分类
    @Modifying
    @Query("UPDATE WikiPage p SET p.categoryId = :targetCategoryId WHERE p.categoryId = :sourceCategoryId")
    int migratePagesToCategory(@Param("sourceCategoryId") Long sourceCategoryId, @Param("targetCategoryId") Long targetCategoryId);
}
