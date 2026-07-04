---
title: 实体关系
description: 数据库 ER 模型 — 8 张表 DDL、JPA 映射、关联关系
domain: architecture
category: data-model
related_sources:
  - AstarosWiki/src/main/java/com/example/astaroswiki/entity/*.java
  - AstarosWiki/src/main/java/com/example/astaroswiki/repository/*.java
  - AstarosWiki/src/main/resources/application.yml
status: final
created: 2026-07-04
updated: 2026-07-04
version: 1.0.0
---

# 实体关系

> 所属：`architecture` | 关联源码：`entity/*.java`、`repository/*.java`

---

## ER 概览

```
users ──1:N──► wiki_pages        (author_id)
users ──1:N──► operation_logs     (user_id)
users ──1:N──► messages           (receiver_id, sender_id)
users ──1:N──► user_requests      (user_id, reviewer_id)

wiki_pages ──N:1──► categories    (category_id)
wiki_pages ──N:M──► tags          (via page_tags)
wiki_pages ──1:N──► page_versions (page_id)
wiki_pages ──1:N──► messages      (related_page_id)

categories ──自引用──► categories (parent_id)
```

---

## 表结构 DDL

### `users`

| 列 | 类型 | 约束 |
|----|------|------|
| `id` | `BIGINT` | PK, AUTO_INCREMENT |
| `username` | `VARCHAR(50)` | UNIQUE, NOT NULL |
| `email` | `VARCHAR(100)` | UNIQUE, NOT NULL |
| `password_hash` | `VARCHAR` | NOT NULL |
| `role` | `VARCHAR` | `ADMIN` / `CREATOR` (default: `CREATOR`) |
| `status` | `VARCHAR` | `ACTIVE` / `PENDING` / `DISABLED` (default: `PENDING`) |
| `created_at` | `DATETIME` | |
| `updated_at` | `DATETIME` | |
| `extra_json` | `JSON` | |

### `wiki_pages`

| 列 | 类型 | 约束 |
|----|------|------|
| `id` | `BIGINT` | PK, AUTO_INCREMENT |
| `title` | `VARCHAR(200)` | NOT NULL |
| `content` | `LONGTEXT` | 已发布内容 |
| `draft_content` | `LONGTEXT` | 草稿内容 |
| `slug` | `VARCHAR(200)` | UNIQUE, NOT NULL |
| `category_id` | `BIGINT` | FK → `categories.id` |
| `author_id` | `BIGINT` | FK → `users.id`, NOT NULL |
| `status` | `VARCHAR` | `SAVED`/`DRAFT`/`PUBLISHED`/`LOCKED`/`DELETED` (default: `PUBLISHED`) |
| `draft_status` | `VARCHAR` | `NULL`/`SAVED`/`DRAFT` |
| `version` | `INT` | default: `1` |
| `view_count` | `INT` | default: `0` |
| `created_at` | `DATETIME` | |
| `updated_at` | `DATETIME` | |
| `draft_updated_at` | `DATETIME` | |
| `last_viewed_at` | `DATETIME` | |
| `reject_reason` | `VARCHAR(500)` | |
| `extra_json` | `JSON` | |

### `page_versions`

| 列 | 类型 | 约束 |
|----|------|------|
| `id` | `BIGINT` | PK, AUTO_INCREMENT |
| `page_id` | `BIGINT` | FK → `wiki_pages.id` |
| `content` | `LONGTEXT` | 该版本内容 |
| `editor_id` | `BIGINT` | FK → `users.id` |
| `version_number` | `INT` | |
| `created_at` | `DATETIME` | |

### `categories`

| 列 | 类型 | 约束 |
|----|------|------|
| `id` | `BIGINT` | PK, AUTO_INCREMENT |
| `name` | `VARCHAR` | NOT NULL |
| `description` | `TEXT` | |
| `parent_id` | `BIGINT` | FK → `categories.id` (自引用) |
| `sort_order` | `INT` | |

### `tags`

| 列 | 类型 | 约束 |
|----|------|------|
| `id` | `BIGINT` | PK, AUTO_INCREMENT |
| `name` | `VARCHAR` | UNIQUE, NOT NULL |

### `page_tags`（关联表）

| 列 | 类型 | 约束 |
|----|------|------|
| `page_id` | `BIGINT` | FK → `wiki_pages.id`, PK |
| `tag_id` | `BIGINT` | FK → `tags.id`, PK |

### `messages`

| 列 | 类型 | 约束 |
|----|------|------|
| `id` | `BIGINT` | PK, AUTO_INCREMENT |
| `sender_id` | `BIGINT` | FK → `users.id` |
| `receiver_id` | `BIGINT` | FK → `users.id`, NOT NULL |
| `title` | `VARCHAR(200)` | |
| `content` | `TEXT` | |
| `related_page_id` | `BIGINT` | FK → `wiki_pages.id` |
| `related_slug` | `VARCHAR(200)` | |
| `is_read` | `BOOLEAN` | default: `false` |
| `opinion` | `TEXT` | 管理员审核意见 |
| `created_at` | `DATETIME` | |

### `user_requests`

| 列 | 类型 | 约束 |
|----|------|------|
| `id` | `BIGINT` | PK, AUTO_INCREMENT |
| `user_id` | `BIGINT` | FK → `users.id` |
| `reviewer_id` | `BIGINT` | FK → `users.id` |
| `request_type` | `VARCHAR` | 如 `CREATOR_REGISTRATION` |
| `status` | `VARCHAR` | `PENDING` / `APPROVED` / `REJECTED` |
| `comment` | `TEXT` | |
| `created_at` | `DATETIME` | |
| `reviewed_at` | `DATETIME` | |

### `operation_logs`

| 列 | 类型 | 约束 |
|----|------|------|
| `id` | `BIGINT` | PK, AUTO_INCREMENT |
| `user_id` | `BIGINT` | FK → `users.id` |
| `action` | `VARCHAR` | 操作类型 |
| `target_type` | `VARCHAR` | 操作目标类型 |
| `target_id` | `BIGINT` | 操作目标 ID |
| `detail` | `TEXT` | 详情 |
| `created_at` | `DATETIME` | |

---

## JPA 映射要点

| 映射 | 实现 |
|------|------|
| 实体注解 | `@Data` + `@Entity` + `@Table(name="...")` |
| 主键 | `@Id` + `@GeneratedValue(strategy = IDENTITY)` |
| 枚举 | `@Enumerated(EnumType.STRING)` |
| 时间戳 | `LocalDateTime` + `@PrePersist` 自动赋值 |
| 多对多 | `@ManyToMany` + `@JoinTable(name="page_tags")` |
| JSON 字段 | `@JdbcTypeCode(SqlTypes.JSON)` 注解 `extraJson` |
| 自增操作 | `@Modifying` + `@Query("UPDATE WikiPage SET viewCount=viewCount+1")` |

---

## 数据库连接

| 配置项 | 值 |
|--------|----|
| JDBC URL | `jdbc:mysql://localhost:3306/astaros_wiki` |
| 用户 | `root` |
| 密码 | ⚠️ `123456`（硬编码） |
| 连接池 | HikariCP (min=5, max=20) |
| DDL 策略 | `update` (开发) / `validate` (生产) |
| Redis | `localhost:6379`, 密码 ⚠️ `123456`（硬编码） |
