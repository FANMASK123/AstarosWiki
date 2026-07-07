package com.example.astaroswiki.service;

import com.example.astaroswiki.dto.page.PageCreateRequest;
import com.example.astaroswiki.dto.page.PageDTO;
import com.example.astaroswiki.dto.page.PageUpdateRequest;
import com.example.astaroswiki.entity.Category;
import com.example.astaroswiki.entity.PageVersion;
import com.example.astaroswiki.entity.Tag;
import com.example.astaroswiki.entity.User;
import com.example.astaroswiki.entity.WikiPage;
import com.example.astaroswiki.enums.PageStatus;
import com.example.astaroswiki.exception.BusinessException;
import com.example.astaroswiki.repository.CategoryRepository;
import com.example.astaroswiki.repository.PageRepository;
import com.example.astaroswiki.repository.PageVersionRepository;
import com.example.astaroswiki.repository.TagRepository;
import com.example.astaroswiki.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PageService {

    private static final int RECENT_PAGE_SIZE = 10;

    private final PageRepository pageRepository;
    private final PageVersionRepository pageVersionRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final MessageService messageService;

    public org.springframework.data.domain.Page<PageDTO> getPublishedPages(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        org.springframework.data.domain.Page<WikiPage> pages =
            pageRepository.findByStatus(PageStatus.PUBLISHED, pageable);
        return pages.map(this::convertToDTO);
    }

    public List<PageDTO> getRecentPages() {
        Pageable pageable = PageRequest.of(0, RECENT_PAGE_SIZE);
        List<WikiPage> pages = pageRepository.findTop10ByStatusOrderByUpdatedAtDesc(
            PageStatus.PUBLISHED, pageable
        );
        return pages.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    public org.springframework.data.domain.Page<PageDTO> getPagesByCategory(
        Long categoryId, int page, int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        List<Long> categoryIds = getAllCategoryIds(categoryId);
        org.springframework.data.domain.Page<WikiPage> pages =
            pageRepository.findByCategoryIdsAndStatus(categoryIds, PageStatus.PUBLISHED, pageable);
        return pages.map(this::convertToDTO);
    }

    private List<Long> getAllCategoryIds(Long categoryId) {
        List<Long> ids = new ArrayList<>();
        ids.add(categoryId);

        List<Category> children = categoryRepository.findByParentIdOrderBySortOrder(categoryId);
        for (Category child : children) {
            ids.addAll(getAllCategoryIds(child.getId()));
        }

        return ids;
    }

    public org.springframework.data.domain.Page<PageDTO> getPagesByTag(
        String tagName, int page, int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        org.springframework.data.domain.Page<WikiPage> pages =
            pageRepository.findByTagNameAndStatus(tagName, PageStatus.PUBLISHED, pageable);
        return pages.map(this::convertToDTO);
    }

    public org.springframework.data.domain.Page<PageDTO> searchPages(
        String keyword, int page, int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        org.springframework.data.domain.Page<WikiPage> pages =
            pageRepository.searchByKeywordAndStatus(keyword, PageStatus.PUBLISHED, pageable);
        return pages.map(this::convertToDTO);
    }

    @Transactional
    public PageDTO getPageBySlug(String slug) {
        WikiPage page = pageRepository.findBySlug(slug)
            .orElseThrow(() -> new BusinessException("页面不存在"));

        if (page.getStatus() == PageStatus.DELETED) {
            throw new BusinessException("页面已被删除");
        }

        if (page.getStatus() == PageStatus.SAVED || page.getStatus() == PageStatus.DRAFT) {
            Long currentUserId = com.example.astaroswiki.util.SecurityUtil.getCurrentUserId();
            if (currentUserId == null
                || (!page.getAuthorId().equals(currentUserId) && !isAdmin(currentUserId))) {
                throw new BusinessException("页面不存在或无权限访问");
            }
        }

        pageRepository.updateViewCount(page.getId());
        page.setViewCount(page.getViewCount() + 1);

        return convertToDTO(page);
    }

    public org.springframework.data.domain.Page<PageDTO> getPagesByAuthor(
        Long authorId, int page, int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(
            Sort.Direction.DESC, "draftUpdatedAt", "updatedAt"
        ));
        org.springframework.data.domain.Page<WikiPage> pages =
            pageRepository.findByAuthorId(authorId, pageable);
        return pages.map(this::convertToDTO);
    }

    public org.springframework.data.domain.Page<PageDTO> getPagesByAuthorAndStatus(
        Long authorId, PageStatus status, int page, int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(
            Sort.Direction.DESC, "draftUpdatedAt", "updatedAt"
        ));
        org.springframework.data.domain.Page<WikiPage> pages =
            pageRepository.findByAuthorIdAndStatus(authorId, status, pageable);
        return pages.map(this::convertToDTO);
    }

    public org.springframework.data.domain.Page<PageDTO> getDraftPages(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        org.springframework.data.domain.Page<WikiPage> pages =
            pageRepository.findPendingReview(PageStatus.DRAFT, PageStatus.DRAFT, pageable);
        return pages.map(this::convertToDTO);
    }

    public org.springframework.data.domain.Page<PageDTO> getSavedPages(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        org.springframework.data.domain.Page<WikiPage> pages =
            pageRepository.findByStatusOrDraftStatus(PageStatus.SAVED, PageStatus.SAVED, pageable);
        return pages.map(this::convertToDTO);
    }

    public org.springframework.data.domain.Page<PageDTO> getPagesByAuthorAndStatusOrDraftStatus(
        Long authorId, PageStatus status, int page, int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(
            Sort.Direction.DESC, "draftUpdatedAt", "updatedAt"
        ));
        org.springframework.data.domain.Page<WikiPage> pages =
            pageRepository.findByAuthorIdAndStatusOrDraftStatus(authorId, status, status, pageable);
        return pages.map(this::convertToDTO);
    }

    @Transactional
    public PageDTO createPage(PageCreateRequest request, Long authorId) {
        return createPage(request, authorId, false);
    }

    @Transactional
    public PageDTO createPage(PageCreateRequest request, Long authorId, boolean publish) {
        String slug = request.getSlug();
        if (!StringUtils.hasText(slug)) {
            slug = generateSlug(request.getTitle());
        }

        if (pageRepository.existsBySlug(slug)) {
            throw new BusinessException("Slug已存在");
        }

        User author = userRepository.findById(authorId)
            .orElseThrow(() -> new BusinessException("用户不存在"));

        WikiPage page = new WikiPage();
        page.setTitle(request.getTitle());
        page.setContent(request.getContent());
        page.setDraftContent(request.getContent());
        page.setSlug(slug);
        page.setAuthorId(authorId);
        page.setStatus(publish ? PageStatus.DRAFT : PageStatus.SAVED);
        page.setVersion(1);

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new BusinessException("分类不存在"));
            page.setCategoryId(request.getCategoryId());
        }

        if (request.getTags() != null && !request.getTags().isEmpty()) {
            Set<Tag> tags = new HashSet<>();
            for (String tagName : request.getTags()) {
                Tag tag = tagRepository.findByName(tagName)
                    .orElseGet(() -> {
                        Tag newTag = new Tag();
                        newTag.setName(tagName);
                        return tagRepository.save(newTag);
                    });
                tags.add(tag);
            }
            page.setTags(tags);
        }

        page = pageRepository.save(page);

        PageVersion version = new PageVersion();
        version.setPageId(page.getId());
        version.setContent(page.getContent());
        version.setEditorId(authorId);
        version.setVersionNumber(1);
        pageVersionRepository.save(version);

        return convertToDTO(page);
    }

    @Transactional
    public PageDTO updatePage(String slug, PageUpdateRequest request, Long userId) {
        return updatePage(slug, request, userId, false);
    }

    @Transactional
    public PageDTO updatePage(String slug, PageUpdateRequest request, Long userId, boolean publish) {
        WikiPage page = pageRepository.findBySlug(slug)
            .orElseThrow(() -> new BusinessException("页面不存在"));

        if (page.getStatus() == PageStatus.LOCKED && !isAdmin(userId)) {
            throw new BusinessException("页面已被锁定，无法编辑");
        }

        if (!(page.getAuthorId().equals(userId) || isAdmin(userId))) {
            throw new BusinessException("无权限编辑此页面");
        }

        PageStatus originalStatus = page.getStatus();

        if (StringUtils.hasText(request.getTitle())) {
            page.setTitle(request.getTitle());
        }

        if (StringUtils.hasText(request.getContent())) {
            page.setDraftContent(request.getContent());

            PageVersion version = new PageVersion();
            version.setPageId(page.getId());
            version.setContent(request.getContent());
            version.setEditorId(userId);
            version.setVersionNumber(page.getVersion() + 1);
            pageVersionRepository.save(version);

            page.setVersion(page.getVersion() + 1);
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new BusinessException("分类不存在"));
            page.setCategoryId(request.getCategoryId());
        }

        if (request.getTags() != null) {
            Set<Tag> tags = new HashSet<>();
            for (String tagName : request.getTags()) {
                Tag tag = tagRepository.findByName(tagName)
                    .orElseGet(() -> {
                        Tag newTag = new Tag();
                        newTag.setName(tagName);
                        return tagRepository.save(newTag);
                    });
                tags.add(tag);
            }
            page.setTags(tags);
        }

        if (originalStatus == PageStatus.PUBLISHED) {
            page.setDraftUpdatedAt(LocalDateTime.now());

            if (publish) {
                page.setDraftStatus(PageStatus.DRAFT);
                page.setRejectReason(null);
            } else {
                page.setDraftStatus(PageStatus.SAVED);
            }
        } else {
            page.setUpdatedAt(LocalDateTime.now());

            if (publish) {
                page.setStatus(PageStatus.DRAFT);
                page.setRejectReason(null);
            } else {
                page.setStatus(PageStatus.SAVED);
            }
        }

        page = pageRepository.save(page);
        return convertToDTO(page);
    }

    @Transactional
    public PageDTO approvePage(String slug, String opinion) {
        WikiPage page = pageRepository.findBySlug(slug)
            .orElseThrow(() -> new BusinessException("页面不存在"));

        boolean hasDraftToApprove = page.getStatus() == PageStatus.DRAFT
            || (page.getStatus() == PageStatus.PUBLISHED
            && page.getDraftStatus() == PageStatus.DRAFT);

        if (!hasDraftToApprove) {
            throw new BusinessException("页面状态不允许审核");
        }

        if (page.getDraftContent() != null) {
            page.setContent(page.getDraftContent());
        }

        page.setStatus(PageStatus.PUBLISHED);
        page.setDraftStatus(null);
        page.setRejectReason(null);
        page.setUpdatedAt(LocalDateTime.now());
        page = pageRepository.save(page);

        messageService.sendMessage(
            page.getAuthorId(),
            "页面审核通过",
            "您的页面「" + page.getTitle() + "」已通过审核并发布。",
            opinion,
            page.getId(),
            page.getSlug()
        );

        return convertToDTO(page);
    }

    @Transactional
    public PageDTO rejectPage(String slug, String rejectReason) {
        WikiPage page = pageRepository.findBySlug(slug)
            .orElseThrow(() -> new BusinessException("页面不存在"));

        boolean hasDraftToReject = page.getStatus() == PageStatus.DRAFT
            || (page.getStatus() == PageStatus.PUBLISHED
            && page.getDraftStatus() == PageStatus.DRAFT);

        if (!hasDraftToReject) {
            throw new BusinessException("页面状态不允许打回");
        }

        page.setRejectReason(rejectReason);

        if (page.getStatus() == PageStatus.PUBLISHED) {
            page.setDraftStatus(PageStatus.SAVED);
            page.setDraftUpdatedAt(LocalDateTime.now());
        } else {
            page.setStatus(PageStatus.SAVED);
            page.setUpdatedAt(LocalDateTime.now());
        }

        page = pageRepository.save(page);

        messageService.sendMessage(
            page.getAuthorId(),
            "页面审核未通过",
            "您的页面「" + page.getTitle() + "」未通过审核。请修改后重新提交。",
            rejectReason != null ? rejectReason : "无",
            page.getId(),
            page.getSlug()
        );

        return convertToDTO(page);
    }

    @Transactional
    public PageDTO unpublishPage(String slug, String reason) {
        WikiPage page = pageRepository.findBySlug(slug)
            .orElseThrow(() -> new BusinessException("页面不存在"));

        if (page.getStatus() != PageStatus.PUBLISHED) {
            throw new BusinessException("页面状态不允许下架");
        }

        page.setStatus(PageStatus.SAVED);
        page.setDraftStatus(null);
        page.setRejectReason(reason);
        page.setUpdatedAt(LocalDateTime.now());
        page = pageRepository.save(page);

        messageService.sendMessage(
            page.getAuthorId(),
            "页面已下架",
            "您的页面「" + page.getTitle() + "」已被下架。请修改后重新提交审核。",
            reason != null ? reason : "无",
            page.getId(),
            page.getSlug()
        );

        return convertToDTO(page);
    }

    @Transactional
    public PageDTO sendAdvice(String slug, String opinion) {
        WikiPage page = pageRepository.findBySlug(slug)
            .orElseThrow(() -> new BusinessException("页面不存在"));

        messageService.sendMessage(
            page.getAuthorId(),
            "页面修改建议",
            "管理员对您的页面「" + page.getTitle() + "」提出了修改建议，请查看详情。",
            opinion,
            page.getId(),
            page.getSlug()
        );

        return convertToDTO(page);
    }

    @Transactional
    public void deletePage(String slug, Long userId) {
        WikiPage page = pageRepository.findBySlug(slug)
            .orElseThrow(() -> new BusinessException("页面不存在"));

        if (!(page.getAuthorId().equals(userId) || isAdmin(userId))) {
            throw new BusinessException("无权限删除此页面");
        }

        page.setStatus(PageStatus.DELETED);
        pageRepository.save(page);
    }

    public List<PageVersion> getPageHistory(String slug) {
        WikiPage page = pageRepository.findBySlug(slug)
            .orElseThrow(() -> new BusinessException("页面不存在"));

        return pageVersionRepository.findByPageIdOrderByVersionNumberDesc(page.getId());
    }

    @Transactional
    public PageDTO rollbackPage(String slug, Integer versionNumber, Long userId) {
        WikiPage page = pageRepository.findBySlug(slug)
            .orElseThrow(() -> new BusinessException("页面不存在"));

        if (!(page.getAuthorId().equals(userId) || isAdmin(userId))) {
            throw new BusinessException("无权限回滚此页面");
        }

        PageVersion version = pageVersionRepository.findByPageIdAndVersionNumber(
            page.getId(), versionNumber
        ).orElseThrow(() -> new BusinessException("版本不存在"));

        if (page.getStatus() == PageStatus.PUBLISHED) {
            page.setDraftContent(version.getContent());
            page.setDraftStatus(PageStatus.SAVED);
            page.setDraftUpdatedAt(LocalDateTime.now());
        } else {
            page.setContent(version.getContent());
            page.setUpdatedAt(LocalDateTime.now());
        }

        page.setVersion(page.getVersion() + 1);

        PageVersion newVersion = new PageVersion();
        newVersion.setPageId(page.getId());
        newVersion.setContent(version.getContent());
        newVersion.setEditorId(userId);
        newVersion.setVersionNumber(page.getVersion());
        pageVersionRepository.save(newVersion);

        page = pageRepository.save(page);
        return convertToDTO(page);
    }

    private String generateSlug(String title) {
        if (!StringUtils.hasText(title)) {
            return "page-" + System.currentTimeMillis();
        }
        String slug = title.toLowerCase()
            .replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5]", "-")
            .replaceAll("-+", "-")
            .replaceAll("^-|-$", "");
        if (!StringUtils.hasText(slug)) {
            slug = "page-" + System.currentTimeMillis();
        }
        return slug;
    }

    private boolean isAdmin(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        return user != null && user.getRole().name().equals("ADMIN");
    }

    private PageDTO convertToDTO(WikiPage page) {
        PageDTO dto = new PageDTO();

        dto.setId(page.getId());
        dto.setTitle(page.getTitle());
        dto.setSlug(page.getSlug());
        dto.setCategoryId(page.getCategoryId());
        dto.setAuthorId(page.getAuthorId());
        dto.setStatus(page.getStatus().name());
        dto.setDraftStatus(page.getDraftStatus() != null ? page.getDraftStatus().name() : null);
        dto.setVersion(page.getVersion());
        dto.setViewCount(page.getViewCount());
        dto.setCreatedAt(page.getCreatedAt());
        dto.setUpdatedAt(page.getUpdatedAt());
        dto.setDraftUpdatedAt(page.getDraftUpdatedAt());

        if (page.getStatus() == PageStatus.PUBLISHED) {
            dto.setContent(page.getContent());
            dto.setDraftContent(page.getDraftContent());
        } else {
            dto.setContent(page.getDraftContent() != null ? page.getDraftContent() : page.getContent());
            dto.setDraftContent(page.getDraftContent());
        }

        if (page.getCategoryId() != null) {
            Category category = categoryRepository.findById(page.getCategoryId()).orElse(null);
            if (category != null) {
                dto.setCategoryName(category.getName());
            }
        }

        User author = userRepository.findById(page.getAuthorId()).orElse(null);
        if (author != null) {
            dto.setAuthorName(author.getUsername());
        }

        Set<String> tagNames = page.getTags()
            .stream()
            .map(Tag::getName)
            .collect(Collectors.toSet());
        dto.setTags(tagNames);

        dto.setRejectReason(page.getRejectReason());

        return dto;
    }
}
