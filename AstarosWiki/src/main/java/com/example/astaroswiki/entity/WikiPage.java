package com.example.astaroswiki.entity;

import com.example.astaroswiki.enums.PageStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "pages")
public class WikiPage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "draft_content", columnDefinition = "LONGTEXT")
    private String draftContent;

    @Column(nullable = false, unique = true, length = 200)
    private String slug;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PageStatus status = PageStatus.PUBLISHED;

    @Column(name = "draft_status")
    private PageStatus draftStatus;

    @Column(nullable = false)
    private Integer version = 1;

    @Column(name = "view_count")
    private Integer viewCount = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "draft_updated_at")
    private LocalDateTime draftUpdatedAt;

    @Column(name = "last_viewed_at")
    private LocalDateTime lastViewedAt;

    @Column(name = "extra_json", columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    private String extraJson;

    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "page_tags",
        joinColumns = @JoinColumn(name = "page_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
}
