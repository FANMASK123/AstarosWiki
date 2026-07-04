---
title: 页面生命周期
description: Wiki 页面状态机与审核工作流 — SAVED → DRAFT → PUBLISHED
domain: architecture
category: business-logic
related_sources:
  - AstarosWiki/src/main/java/com/example/astaroswiki/service/PageService.java
  - AstarosWiki/src/main/java/com/example/astaroswiki/entity/WikiPage.java
  - AstarosWiki/src/main/java/com/example/astaroswiki/entity/PageVersion.java
  - AstarosWiki/src/main/java/com/example/astaroswiki/enums/PageStatus.java
  - AstarosWiki/src/main/java/com/example/astaroswiki/controller/PageController.java
status: final
created: 2026-07-04
updated: 2026-07-04
version: 1.0.0
---

# 页面生命周期

> 所属：`architecture` | 关联源码：`service/PageService.java`、`entity/WikiPage.java`、`enums/PageStatus.java`

---

## 状态定义

| 状态 | 枚举值 | 含义 |
|------|--------|------|
| 草稿 | `SAVED` | 已保存，未提交审核 |
| 待审核 | `DRAFT` | 已提交审核，等待管理员处理 |
| 已发布 | `PUBLISHED` | 审核通过，公开可见 |
| 已锁定 | `LOCKED` | 管理员锁定，禁止编辑 |
| 已删除 | `DELETED` | 软删除，仅管理员可见 |

### 辅助字段

| 字段 | 类型 | 说明 |
|------|------|------|
| `draftStatus` | `PageStatus?` | 独立草稿审核状态 (`null` / `SAVED` / `DRAFT`) |
| `draftContent` | `LONGTEXT` | 草稿内容，审核通过后复制到 `content` |
| `draftUpdatedAt` | `LocalDateTime` | 草稿更新时间，不影响已发布页面的 `updatedAt` |

---

## 状态机

```
新建页面:
  [创建] ──► SAVED ──[提交审核]──► DRAFT ──[审核通过]──► PUBLISHED
               ▲                      │
               └────[打回]────────────┘

编辑已发布页面:
  PUBLISHED ──[编辑保存]──► draftStatus=SAVED  (主 status 保持 PUBLISHED)
            ──[提交审核]──► draftStatus=DRAFT  (主 status 保持 PUBLISHED)
            ──[审核通过]──► draftContent→content, draftStatus=null
            ──[打回]──────► draftStatus=SAVED

删除:
  任意状态 ──[删除]──► DELETED （软删除）
  DELETED  ──[恢复]──► SAVED
  DELETED  ──[永久删除]──► 物理删除

锁定:
  任意状态 ──[锁定]──► LOCKED （禁止非管理员编辑）
  LOCKED   ──[解锁]──► 恢复原状态
```

---

## PageService 核心方法

### `createPage(PageCreateRequest, authorId, publish)`

- `publish=false` → `status = SAVED`
- `publish=true` → `status = DRAFT`（直接提交审核）
- 自动创建 `PageVersion(v1)`
- 标签不存在则自动创建（`tagRepository.findByName().orElseGet(...)`）

### `updatePage(slug, PageUpdateRequest, userId, publish)`

- **权限检查**：`authorId == userId || isAdmin(userId)`
- **锁定检查**：`status == LOCKED && !isAdmin` → 拒绝
- **已发布页面**：写入 `draftContent`，主 `content` 不变
- **非已发布**：直接写入 `content`
- `publish=true` → 提交审核
- `publish=false` → 保存草稿
- 自动递增 `version`，创建 `PageVersion`

### `approvePage(slug, opinion)`

- 检查：`status == DRAFT || (status == PUBLISHED && draftStatus == DRAFT)`
- 操作：`draftContent` → `content`，`status = PUBLISHED`，`draftStatus = null`
- 发送站内消息通知作者

### `rejectPage(slug, rejectReason)`

- 已发布页面草稿被驳：主 `status` 保持 `PUBLISHED`，`draftStatus = SAVED`
- 新建页面被驳：`status = SAVED`
- 发送站内消息通知作者

### `deletePage(slug, userId)`

- 软删除：`status = DELETED`
- 权限：`authorId == userId || isAdmin`

### `rollbackPage(slug, versionNumber, userId)`

- 回滚到指定版本
- 已发布页面：写入 `draftContent`，`draftStatus = SAVED`
- 非已发布：写入 `content`
- 创建新 `PageVersion` 记录回滚操作

### `getPageBySlug(slug)`

- 访问控制：`SAVED` / `DRAFT` 只有作者和管理员可见
- `DELETED` 状态返回 404
- 自动递增 `viewCount`

---

## WikiPage 实体字段

| 字段 | 类型 | 约束 |
|------|------|------|
| `id` | `Long` | PK, AUTO |
| `title` | `String(200)` | NOT NULL |
| `content` | `LONGTEXT` | 已发布内容 |
| `draftContent` | `LONGTEXT` | 编辑中草稿 |
| `slug` | `String(200)` | UNIQUE, NOT NULL |
| `categoryId` | `Long` | FK → categories |
| `authorId` | `Long` | FK → users, NOT NULL |
| `status` | `PageStatus` | 默认 `PUBLISHED` |
| `draftStatus` | `PageStatus?` | 草稿审核状态 |
| `version` | `int` | 默认 `1` |
| `viewCount` | `int` | 默认 `0` |
| `createdAt` | `LocalDateTime` | `@PrePersist` 自动 |
| `updatedAt` | `LocalDateTime` | |
| `draftUpdatedAt` | `LocalDateTime` | 草稿更新时间 |
| `lastViewedAt` | `LocalDateTime` | |
| `rejectReason` | `String(500)` | 审核打回原因 |
| `extraJson` | `JSON` | 扩展字段 |
| `tags` | `Set<Tag>` | `@ManyToMany` via `page_tags` |

---

## PageVersion 实体字段

| 字段 | 类型 | 约束 |
|------|------|------|
| `id` | `Long` | PK, AUTO |
| `pageId` | `Long` | FK → wiki_pages |
| `content` | `LONGTEXT` | 该版本内容 |
| `editorId` | `Long` | FK → users |
| `versionNumber` | `int` | 版本号 |
| `createdAt` | `LocalDateTime` | 创建时间 |

---

## 内容返回逻辑（convertToDTO）

| 页面状态 | `content` 返回 | `draftContent` 返回 |
|---------|---------------|-------------------|
| `PUBLISHED` | 已发布内容 | 草稿内容（分开展示） |
| 其他 | `draftContent ?? content` | `draftContent` |

---

## 搜索

```
searchPages(keyword, page, size)
  → JPQL: WHERE (p.content LIKE %kw% OR p.draftContent LIKE %kw%)
          AND p.status = 'PUBLISHED'
  → 只返回已发布页面
```
