package com.example.astaroswiki.service;

import com.example.astaroswiki.dto.page.PageCreateRequest;
import com.example.astaroswiki.dto.page.PageDTO;
import com.example.astaroswiki.entity.PageVersion;
import com.example.astaroswiki.entity.User;
import com.example.astaroswiki.entity.WikiPage;
import com.example.astaroswiki.enums.PageStatus;
import com.example.astaroswiki.enums.UserRole;
import com.example.astaroswiki.exception.BusinessException;
import com.example.astaroswiki.repository.CategoryRepository;
import com.example.astaroswiki.repository.PageRepository;
import com.example.astaroswiki.repository.PageVersionRepository;
import com.example.astaroswiki.repository.TagRepository;
import com.example.astaroswiki.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PageService")
class PageServiceTest {

    @Mock private PageRepository pageRepository;
    @Mock private PageVersionRepository pageVersionRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private TagRepository tagRepository;
    @Mock private UserRepository userRepository;
    @Mock private MessageService messageService;

    @InjectMocks
    private PageService pageService;

    private WikiPage wikiPage;
    private User author;
    private User admin;

    @BeforeEach
    void setUp() {
        author = new User();
        author.setId(1L);
        author.setUsername("author");
        author.setRole(UserRole.CREATOR);

        admin = new User();
        admin.setId(2L);
        admin.setUsername("admin");
        admin.setRole(UserRole.ADMIN);

        wikiPage = new WikiPage();
        wikiPage.setId(100L);
        wikiPage.setTitle("Test Page");
        wikiPage.setContent("Content");
        wikiPage.setSlug("test-page");
        wikiPage.setAuthorId(1L);
        wikiPage.setStatus(PageStatus.PUBLISHED);
        wikiPage.setVersion(1);
        wikiPage.setViewCount(0);
    }

    @Nested
    @DisplayName("getPublishedPages")
    class PublishedPages {

        @Test
        @DisplayName("返回已发布页面分页列表")
        void shouldReturnPublishedPages() {
            Page<WikiPage> page = new PageImpl<>(List.of(wikiPage));
            when(pageRepository.findByStatus(eq(PageStatus.PUBLISHED), any(PageRequest.class)))
                .thenReturn(page);

            Page<PageDTO> result = pageService.getPublishedPages(0, 10);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getTitle()).isEqualTo("Test Page");
        }
    }

    @Nested
    @DisplayName("getPageBySlug")
    class GetBySlug {

        @Test
        @DisplayName("找到已发布页面 → 返回 DTO")
        void shouldReturnPageWhenPublished() {
            when(pageRepository.findBySlug("test-page")).thenReturn(Optional.of(wikiPage));

            PageDTO dto = pageService.getPageBySlug("test-page");

            assertThat(dto.getTitle()).isEqualTo("Test Page");
            verify(pageRepository).updateViewCount(100L);
        }

        @Test
        @DisplayName("页面已删除 → 抛异常")
        void shouldThrowWhenDeleted() {
            wikiPage.setStatus(PageStatus.DELETED);
            when(pageRepository.findBySlug("test-page")).thenReturn(Optional.of(wikiPage));

            assertThatThrownBy(() -> pageService.getPageBySlug("test-page"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("已被删除");
        }

        @Test
        @DisplayName("页面不存在 → 抛异常")
        void shouldThrowWhenNotFound() {
            when(pageRepository.findBySlug("missing")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> pageService.getPageBySlug("missing"))
                .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("createPage")
    class Create {

        @Test
        @DisplayName("保存草稿 → status=SAVED, 创建 v1 版本记录")
        void shouldSaveAsDraft() {
            PageCreateRequest req = new PageCreateRequest();
            req.setTitle("New");
            req.setContent("Hello");
            req.setSlug("new-page");

            when(pageRepository.existsBySlug("new-page")).thenReturn(false);
            when(userRepository.findById(1L)).thenReturn(Optional.of(author));
            when(pageRepository.save(any(WikiPage.class))).thenAnswer(inv -> {
                WikiPage p = inv.getArgument(0);
                p.setId(200L);
                return p;
            });

            PageDTO dto = pageService.createPage(req, 1L, false);

            assertThat(dto.getStatus()).isEqualTo("SAVED");
            verify(pageVersionRepository).save(any(PageVersion.class));
        }

        @Test
        @DisplayName("提交审核 → status=DRAFT")
        void shouldSubmitForReview() {
            PageCreateRequest req = new PageCreateRequest();
            req.setTitle("New");
            req.setContent("Hello");
            req.setSlug("review-page");

            when(pageRepository.existsBySlug("review-page")).thenReturn(false);
            when(userRepository.findById(1L)).thenReturn(Optional.of(author));
            when(pageRepository.save(any(WikiPage.class))).thenAnswer(inv -> {
                WikiPage p = inv.getArgument(0);
                p.setId(201L);
                return p;
            });

            PageDTO dto = pageService.createPage(req, 1L, true);

            assertThat(dto.getStatus()).isEqualTo("DRAFT");
        }

        @Test
        @DisplayName("Slug 已存在 → 抛异常")
        void shouldThrowWhenSlugTaken() {
            PageCreateRequest req = new PageCreateRequest();
            req.setSlug("taken");
            when(pageRepository.existsBySlug("taken")).thenReturn(true);

            assertThatThrownBy(() -> pageService.createPage(req, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Slug已存在");
        }
    }

    @Nested
    @DisplayName("deletePage")
    class Delete {

        @Test
        @DisplayName("作者删除 → 软删除 status=DELETED")
        void shouldSoftDeleteByAuthor() {
            when(pageRepository.findBySlug("test-page")).thenReturn(Optional.of(wikiPage));

            pageService.deletePage("test-page", 1L);

            verify(pageRepository).save(any(WikiPage.class));
        }

        @Test
        @DisplayName("非作者删除 → 抛异常")
        void shouldRejectNonAuthor() {
            when(pageRepository.findBySlug("test-page")).thenReturn(Optional.of(wikiPage));

            assertThatThrownBy(() -> pageService.deletePage("test-page", 999L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("无权限");
        }

        @Test
        @DisplayName("管理员删除他人页面 → 通过")
        void shouldAllowAdminDelete() {
            when(pageRepository.findBySlug("test-page")).thenReturn(Optional.of(wikiPage));
            when(userRepository.findById(2L)).thenReturn(Optional.of(admin));

            pageService.deletePage("test-page", 2L);

            verify(pageRepository).save(any(WikiPage.class));
        }
    }
}
