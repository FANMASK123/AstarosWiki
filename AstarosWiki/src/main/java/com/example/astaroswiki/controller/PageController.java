package com.example.astaroswiki.controller;

import com.example.astaroswiki.common.PageResult;
import com.example.astaroswiki.common.Result;
import com.example.astaroswiki.dto.page.PageCreateRequest;
import com.example.astaroswiki.dto.page.PageDTO;
import com.example.astaroswiki.dto.page.PageUpdateRequest;
import com.example.astaroswiki.entity.PageVersion;
import com.example.astaroswiki.enums.PageStatus;
import com.example.astaroswiki.service.PageService;
import com.example.astaroswiki.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "页面管理", description = "页面相关接口")
@RestController
@RequestMapping("/api/pages")
@RequiredArgsConstructor
public class PageController {

    private final PageService pageService;

    @Operation(summary = "获取最新页面列表")
    @GetMapping("/recent")
    public Result<List<PageDTO>> getRecentPages() {
        List<PageDTO> pages = pageService.getRecentPages();
        return Result.success(pages);
    }

    @Operation(summary = "分页获取已发布页面")
    @GetMapping
    public Result<PageResult<PageDTO>> getPublishedPages(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Page<PageDTO> pages = pageService.getPublishedPages(page, size);
        return Result.success(PageResult.of(pages));
    }

    @Operation(summary = "分页获取草稿页面")
    @GetMapping("/drafts")
    @PreAuthorize("hasAnyRole('ADMIN', 'CREATOR')")
    public Result<PageResult<PageDTO>> getDraftPages(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Page<PageDTO> pages = pageService.getDraftPages(page, size);
        return Result.success(PageResult.of(pages));
    }

    @Operation(summary = "分页获取保存态页面")
    @GetMapping("/saved")
    @PreAuthorize("hasAnyRole('ADMIN', 'CREATOR')")
    public Result<PageResult<PageDTO>> getSavedPages(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Page<PageDTO> pages = pageService.getSavedPages(page, size);
        return Result.success(PageResult.of(pages));
    }

    @Operation(summary = "按分类获取页面")
    @GetMapping("/category/{categoryId}")
    public Result<PageResult<PageDTO>> getPagesByCategory(
        @PathVariable Long categoryId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Page<PageDTO> pages = pageService.getPagesByCategory(categoryId, page, size);
        return Result.success(PageResult.of(pages));
    }

    @Operation(summary = "按标签获取页面")
    @GetMapping("/tag/{tagName}")
    public Result<PageResult<PageDTO>> getPagesByTag(
        @PathVariable String tagName,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Page<PageDTO> pages = pageService.getPagesByTag(tagName, page, size);
        return Result.success(PageResult.of(pages));
    }

    @Operation(summary = "搜索页面")
    @GetMapping("/search")
    public Result<PageResult<PageDTO>> searchPages(
        @RequestParam String keyword,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Page<PageDTO> pages = pageService.searchPages(keyword, page, size);
        return Result.success(PageResult.of(pages));
    }

    @Operation(summary = "获取当前用户的所有页面")
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('ADMIN', 'CREATOR')")
    public Result<PageResult<PageDTO>> getMyPages(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return Result.error("未登录");
        }
        Page<PageDTO> pages = pageService.getPagesByAuthor(userId, page, size);
        return Result.success(PageResult.of(pages));
    }

    @Operation(summary = "获取页面详情")
    @GetMapping("/{slug}")
    public Result<PageDTO> getPageBySlug(@PathVariable String slug) {
        PageDTO page = pageService.getPageBySlug(slug);
        return Result.success(page);
    }

    @Operation(summary = "获取用户的页面")
    @GetMapping("/author/{authorId}")
    public Result<PageResult<PageDTO>> getPagesByAuthor(
        @PathVariable Long authorId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Page<PageDTO> pages = pageService.getPagesByAuthor(authorId, page, size);
        return Result.success(PageResult.of(pages));
    }

    @Operation(summary = "获取用户指定状态的页面")
    @GetMapping("/author/{authorId}/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CREATOR')")
    public Result<PageResult<PageDTO>> getPagesByAuthorAndStatus(
        @PathVariable Long authorId,
        @PathVariable String status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        PageStatus pageStatus = PageStatus.valueOf(status.toUpperCase());
        Page<PageDTO> pages = pageService.getPagesByAuthorAndStatus(authorId, pageStatus, page, size);
        return Result.success(PageResult.of(pages));
    }

    @Operation(summary = "创建页面")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CREATOR')")
    public Result<PageDTO> createPage(@Valid @RequestBody PageCreateRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return Result.error("未登录");
        }
        boolean publish = request.getPublish() != null && request.getPublish();
        PageDTO page = pageService.createPage(request, userId, publish);
        return Result.success(page);
    }

    @Operation(summary = "更新页面")
    @PutMapping("/{slug}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CREATOR')")
    public Result<PageDTO> updatePage(
        @PathVariable String slug,
        @Valid @RequestBody PageUpdateRequest request
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return Result.error("未登录");
        }
        boolean publish = request.getPublish() != null && request.getPublish();
        PageDTO page = pageService.updatePage(slug, request, userId, publish);
        return Result.success(page);
    }

    @Operation(summary = "审核页面")
    @PostMapping("/{slug}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageDTO> approvePage(
        @PathVariable String slug,
        @RequestBody(required = false) java.util.Map<String, String> body
    ) {
        String opinion = body != null ? body.get("opinion") : null;
        PageDTO page = pageService.approvePage(slug, opinion);
        return Result.success(page);
    }

    @Operation(summary = "打回页面")
    @PostMapping("/{slug}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageDTO> rejectPage(
        @PathVariable String slug,
        @RequestBody(required = false) java.util.Map<String, String> body
    ) {
        String rejectReason = body != null ? body.get("rejectReason") : null;
        PageDTO page = pageService.rejectPage(slug, rejectReason);
        return Result.success(page);
    }

    @Operation(summary = "下架页面")
    @PostMapping("/{slug}/unpublish")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageDTO> unpublishPage(
        @PathVariable String slug,
        @RequestBody(required = false) java.util.Map<String, String> body
    ) {
        String reason = body != null ? body.get("reason") : null;
        PageDTO page = pageService.unpublishPage(slug, reason);
        return Result.success(page);
    }

    @Operation(summary = "发送修改建议")
    @PostMapping("/{slug}/advice")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageDTO> sendAdvice(
        @PathVariable String slug,
        @RequestBody(required = false) java.util.Map<String, String> body
    ) {
        String opinion = body != null ? body.get("opinion") : null;
        PageDTO page = pageService.sendAdvice(slug, opinion);
        return Result.success(page);
    }

    @Operation(summary = "获取当前用户的草稿页面")
    @GetMapping("/my/drafts")
    @PreAuthorize("hasAnyRole('ADMIN', 'CREATOR')")
    public Result<PageResult<PageDTO>> getMyDraftPages(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return Result.error("未登录");
        }
        Page<PageDTO> pages = pageService.getPagesByAuthorAndStatusOrDraftStatus(
            userId, PageStatus.DRAFT, page, size
        );
        return Result.success(PageResult.of(pages));
    }

    @Operation(summary = "获取当前用户的已发布页面")
    @GetMapping("/my/published")
    @PreAuthorize("hasAnyRole('ADMIN', 'CREATOR')")
    public Result<PageResult<PageDTO>> getMyPublishedPages(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return Result.error("未登录");
        }
        Page<PageDTO> pages = pageService.getPagesByAuthorAndStatus(
            userId, PageStatus.PUBLISHED, page, size
        );
        return Result.success(PageResult.of(pages));
    }

    @Operation(summary = "获取当前用户的保存页面")
    @GetMapping("/my/saved")
    @PreAuthorize("hasAnyRole('ADMIN', 'CREATOR')")
    public Result<PageResult<PageDTO>> getMySavedPages(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return Result.error("未登录");
        }
        Page<PageDTO> pages = pageService.getPagesByAuthorAndStatusOrDraftStatus(
            userId, PageStatus.SAVED, page, size
        );
        return Result.success(PageResult.of(pages));
    }

    @Operation(summary = "删除页面")
    @DeleteMapping("/{slug}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CREATOR')")
    public Result<Void> deletePage(@PathVariable String slug) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return Result.error("未登录");
        }
        pageService.deletePage(slug, userId);
        return Result.success();
    }

    @Operation(summary = "获取页面历史版本")
    @GetMapping("/{slug}/history")
    public Result<List<PageVersion>> getPageHistory(@PathVariable String slug) {
        List<PageVersion> versions = pageService.getPageHistory(slug);
        return Result.success(versions);
    }

    @Operation(summary = "回滚页面版本")
    @PostMapping("/{slug}/rollback/{versionNumber}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CREATOR')")
    public Result<PageDTO> rollbackPage(
        @PathVariable String slug,
        @PathVariable Integer versionNumber
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return Result.error("未登录");
        }
        PageDTO page = pageService.rollbackPage(slug, versionNumber, userId);
        return Result.success(page);
    }
}
