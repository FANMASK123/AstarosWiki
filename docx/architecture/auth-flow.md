---
title: 认证流程
description: JWT 无状态认证体系 — 过滤器链、权限矩阵、端点清单
domain: architecture
category: security
related_sources:
  - AstarosWiki/src/main/java/com/example/astaroswiki/config/SecurityConfig.java
  - AstarosWiki/src/main/java/com/example/astaroswiki/util/JwtUtil.java
  - AstarosWiki/src/main/java/com/example/astaroswiki/security/JwtAuthenticationFilter.java
  - AstarosWiki/src/main/java/com/example/astaroswiki/service/AuthService.java
  - AstarosWiki/src/main/java/com/example/astaroswiki/controller/AuthController.java
  - AstarosWiki/src/main/resources/application.yml
status: final
created: 2026-07-04
updated: 2026-07-04
version: 1.0.0
---

# 认证流程

> 所属：`architecture` | 关联源码：`config/SecurityConfig.java`、`util/JwtUtil.java`、`security/JwtAuthenticationFilter.java`

---

## 架构概览

```
客户端                              Spring Security 过滤器链
──────                              ────────────────────────

POST /api/auth/login ─────────────► AuthController.login()
                                        │
                                        ▼
                                    AuthService.login()
                                        │ 1. 查用户 (userRepository.findByUsername)
                                        │ 2. 验密码 (BCrypt.matches)
                                        │ 3. 检状态 (PENDING / DISABLED)
                                        │ 4. 生成 JWT (jwtUtil.generateToken)
                                        ▼
返回 { token, username, role, userId }


后续请求:
GET /api/xxx
Header: Authorization: Bearer <token>
                                        │
                                        ▼
                                    JwtAuthenticationFilter.doFilterInternal()
                                        │ 1. 提取 Token (getTokenFromRequest)
                                        │ 2. 验证有效性 (jwtUtil.validateToken)
                                        │ 3. 解析 userId/username/role
                                        │ 4. 注入 SecurityContext
                                        ▼
                                    Controller
                                    (可通过 SecurityUtil.getCurrentUserId() 获取用户)
```

---

## JWT 结构

| 字段 | 类型 | 说明 |
|------|------|------|
| `userId` | `Long` | 用户 ID |
| `username` | `String` | 用户名 |
| `role` | `String` | 角色 (`ADMIN` / `CREATOR`) |
| `sub` | `String` | 主题 (= username) |
| `iat` | `Date` | 签发时间 |
| `exp` | `Date` | 过期时间 = `iat + jwt.expiration` (默认 `86400000ms` = 24h) |

- **签名算法**：HMAC-SHA
- **密钥来源**：`application.yml` → `jwt.secret`
- ⚠️ 当前密钥硬编码：`astaros-wiki-secret-key-for-jwt-token-generation-and-validation-2024`

---

## 端点清单

| 方法 | 路径 | 认证 | 说明 |
|------|------|:--:|------|
| `POST` | `/api/auth/login` | 无 | 登录，返回 JWT |
| `POST` | `/api/auth/register` | 无 | 注册，创建 `PENDING` 用户 + 申请记录 |
| `POST` | `/api/auth/reset-admin-password` | ⚠️ **无** | 直接重置管理员密码为 `admin123` |

---

## 权限层级

### URL 级（SecurityConfig）

| 规则 | 路径 | 说明 |
|------|------|------|
| `permitAll()` | `/api/auth/**` | 登录、注册 |
| `permitAll()` | `/api/pages/**` | 浏览公开页面 |
| `permitAll()` | `/api/categories/**` | 浏览分类 |
| `permitAll()` | `/api/tags/**` | 浏览标签 |
| `permitAll()` | `/swagger-ui/**`, `/v3/api-docs/**`, `/doc.html` | API 文档 |
| `authenticated()` | `/api/users/**` | 用户操作 |
| `hasRole('ADMIN')` | `/api/admin/**` | 管理功能 |
| `authenticated()` | 其他所有 | 兜底 |

### 方法级（@PreAuthorize）

| 注解 | 使用场景 |
|------|---------|
| `@PreAuthorize("hasAnyRole('ADMIN', 'CREATOR')")` | 页面创建/编辑/删除 |
| `@PreAuthorize("hasRole('ADMIN')")` | 页面审核/下架/管理 |

---

## 角色与状态

| 枚举 | 值 | 说明 |
|------|----|------|
| **UserRole** | `ADMIN` | 管理员 |
| | `CREATOR` | 创作者（注册默认） |
| **UserStatus** | `PENDING` | 待审核 |
| | `ACTIVE` | 正常 |
| | `DISABLED` | 已禁用 |

---

## SecurityUtil 方法

```java
// 从 SecurityContext 获取当前用户 ID
Long userId = SecurityUtil.getCurrentUserId();

// 实现原理：
// SecurityContextHolder.getContext().getAuthentication().getPrincipal()
// → 返回 userId (Long)，未登录返回 null
```

---

## 密码策略

| 项目 | 值 |
|------|----|
| 算法 | BCrypt (`BCryptPasswordEncoder`，自动加盐) |
| 存储字段 | `users.password_hash` |
| 验证方式 | `passwordEncoder.matches(rawPassword, storedHash)` |
| ⚠️ 复杂度要求 | 无（注册时可设任意长度密码） |

---

## 已知缺陷

| # | 严重度 | 问题 |
|---|:---:|------|
| 1 | 🔴 | JWT 密钥硬编码 — 应改为环境变量 `JWT_SECRET` |
| 2 | 🟠 | 无 Refresh Token — 过期需重新登录 |
| 3 | 🟠 | 无 Token 黑名单 — 无法主动失效 |
| 4 | 🔴 | `/reset-admin-password` 无认证 — 任何人可重置管理员密码 |
| 5 | 🟡 | Token 存 localStorage — XSS 可窃取 |
