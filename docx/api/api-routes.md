---
title: API 路由表
description: 完整端点清单 — 30+ 路由、请求/响应格式、前端调用层映射
domain: api
category: reference
related_sources:
  - AstarosWiki/src/main/java/com/example/astaroswiki/controller/*.java
  - a-staros-wiki-frontend/src/api/*.ts
  - a-staros-wiki-frontend/src/api/request.ts
status: final
created: 2026-07-04
updated: 2026-07-04
version: 1.0.0
---

# API 路由表

> 所属：`api` | 关联源码：`controller/*.java`、前端 `api/*.ts`

---

## 基础信息

| 项目 | 值 |
|------|----|
| Base URL | `http://localhost:8801` |
| 前端代理 | `/api` → `http://localhost:8801`（vite.config.ts） |
| Content-Type | `application/json` |
| 认证方式 | `Header: Authorization: Bearer <token>` |

---

## 统一响应格式

```typescript
// 后端: common/Result.java
Result<T> {
  code: number    // 200=成功, 400=参数错误, 500=服务器错误
  msg:  string    // 提示消息
  data: T         // 泛型数据
}

// 后端: common/PageResult.java
PageResult<T> {
  content:    T[]      // 数据列表
  total:      number   // 总记录数
  page:       number   // 当前页码 (0-based)
  size:       number   // 每页大小
  totalPages: number   // 总页数
}
```

---

## 认证 `/api/auth`

| 方法 | 路径 | Body / Params | 返回 | 认证 |
|------|------|--------------|------|:--:|
| `POST` | `/api/auth/login` | `{ username, password }` | `AuthResponse { token, username, role, userId }` | — |
| `POST` | `/api/auth/register` | `{ username, email, password }` | `Void` | — |
| `POST` | `/api/auth/reset-admin-password` | — | `String` | ⚠️ 无 |

---

## 页面 `/api/pages`

| 方法 | 路径 | 参数 | 返回 | 认证 |
|------|------|------|------|:--:|
| `GET` | `/api/pages/recent` | — | `List<PageDTO>` (最新10条) | — |
| `GET` | `/api/pages` | `?page=&size=` | `PageResult<PageDTO>` | — |
| `GET` | `/api/pages/drafts` | `?page=&size=` | `PageResult<PageDTO>` | CREATOR+ |
| `GET` | `/api/pages/saved` | `?page=&size=` | `PageResult<PageDTO>` | CREATOR+ |
| `GET` | `/api/pages/category/{id}` | `?page=&size=` | `PageResult<PageDTO>` | — |
| `GET` | `/api/pages/tag/{name}` | `?page=&size=` | `PageResult<PageDTO>` | — |
| `GET` | `/api/pages/search` | `?keyword=&page=&size=` | `PageResult<PageDTO>` | — |
| `GET` | `/api/pages/my` | `?page=&size=` | `PageResult<PageDTO>` | CREATOR+ |
| `GET` | `/api/pages/my/drafts` | `?page=&size=` | `PageResult<PageDTO>` | CREATOR+ |
| `GET` | `/api/pages/my/published` | `?page=&size=` | `PageResult<PageDTO>` | CREATOR+ |
| `GET` | `/api/pages/my/saved` | `?page=&size=` | `PageResult<PageDTO>` | CREATOR+ |
| `GET` | `/api/pages/{slug}` | — | `PageDTO`（自动 +1 浏览） | — |
| `GET` | `/api/pages/author/{id}` | `?page=&size=` | `PageResult<PageDTO>` | — |
| `GET` | `/api/pages/author/{id}/status/{s}` | `?page=&size=` | `PageResult<PageDTO>` | CREATOR+ |
| `GET` | `/api/pages/{slug}/history` | — | `List<PageVersion>` | — |
| `POST` | `/api/pages` | `PageCreateRequest` | `PageDTO` | CREATOR+ |
| `PUT` | `/api/pages/{slug}` | `PageUpdateRequest` | `PageDTO` | CREATOR+ |
| `POST` | `/api/pages/{slug}/approve` | `{ opinion? }` | `PageDTO` | ADMIN |
| `POST` | `/api/pages/{slug}/reject` | `{ rejectReason? }` | `PageDTO` | ADMIN |
| `POST` | `/api/pages/{slug}/unpublish` | `{ reason? }` | `PageDTO` | ADMIN |
| `POST` | `/api/pages/{slug}/advice` | `{ opinion }` | `PageDTO` | ADMIN |
| `POST` | `/api/pages/{slug}/rollback/{v}` | — | `PageDTO` | CREATOR+ |
| `DELETE` | `/api/pages/{slug}` | — | `Void`（软删除） | CREATOR+ |

---

## 管理 `/api/admin`（全部需 `ADMIN`）

| 方法 | 路径 | 参数 | 返回 |
|------|------|------|------|
| `GET` | `/api/admin/users` | `?page=&size=` | `PageResult<User>` |
| `GET` | `/api/admin/requests` | `?page=&size=` | `PageResult<UserRequest>` |
| `POST` | `/api/admin/requests/{id}/approve` | `?comment=` | `Void` |
| `POST` | `/api/admin/requests/{id}/reject` | `?comment=` | `Void` |
| `POST` | `/api/admin/users/{id}/disable` | — | `Void` |
| `POST` | `/api/admin/users/{id}/enable` | — | `Void` |
| `DELETE` | `/api/admin/users/{id}` | — | `Void` |
| `GET` | `/api/admin/pages/deleted` | `?page=&size=` | `PageResult<PageDTO>` |
| `POST` | `/api/admin/pages/{id}/restore` | — | `Void` |
| `DELETE` | `/api/admin/pages/{id}/permanent` | — | `Void` |
| `POST` | `/api/admin/pages/{id}/lock` | — | `Void` |
| `POST` | `/api/admin/pages/{id}/unlock` | — | `Void` |
| `GET` | `/api/admin/logs` | `?page=&size=` | `PageResult<OperationLog>` |

---

## 分类 `/api/categories`

| 方法 | 路径 | 返回 | 认证 |
|------|------|------|:--:|
| `GET` | `/api/categories` | 树形 `List<CategoryDTO>`（含 children） | — |
| `GET` | `/api/categories/{id}` | `CategoryDTO` | — |

---

## 标签 `/api/tags`

| 方法 | 路径 | 返回 | 认证 |
|------|------|------|:--:|
| `GET` | `/api/tags` | `List<Tag>` | — |

---

## 用户 `/api/users`

| 方法 | 路径 | 返回 | 认证 |
|------|------|------|:--:|
| `GET` | `/api/users/me` | `User` | ✓ |

---

## 消息 `/api/messages`

| 方法 | 路径 | 返回 | 认证 |
|------|------|------|:--:|
| `GET` | `/api/messages` | `?page=&size=` | ✓ |
| `GET` | `/api/messages/unread/count` | `int` | ✓ |
| `PUT` | `/api/messages/{id}/read` | — | ✓ |
| `PUT` | `/api/messages/read/all` | — | ✓ |

---

## 前端 API 调用层

**文件**：`src/api/request.ts`

```typescript
// Axios 实例
const service = axios.create({ baseURL: '/api', timeout: 10000 })

// 请求拦截器
service.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// 响应拦截器
service.interceptors.response.use(
  response => {
    if (response.data.code !== 200) { ElMessage.error(...); return Promise.reject(...) }
    return response.data
  },
  error => {
    if (error.response?.status === 401) { /* 清除登录态, 跳转 /login */ }
    else if (error.response?.status === 403) { ElMessage.error('无权限访问') }
    else { ElMessage.error(...) }
    return Promise.reject(error)
  }
)

// 封装方法
export function get<T>(url, params?)   → service.get(url, { params })
export function post<T>(url, data?)    → service.post(url, data)
export function put<T>(url, data?)     → service.put(url, data)
export function deleteRequest<T>(url)  → service.delete(url)
```

**按域分文件**：

| 文件 | 主要函数 |
|------|---------|
| `api/auth.ts` | `login()`, `register()` |
| `api/page.ts` | `getRecentPages()`, `getPageBySlug()`, `createPage()`, `updatePage()`, `approvePage()`, `rejectPage()`, `deletePage()`, `rollbackPage()`, `searchPages()`, ... |
| `api/admin.ts` | `getUsers()`, `approveRequest()`, `rejectRequest()`, `disableUser()`, `enableUser()`, `deleteUser()`, ... |
| `api/category.ts` | `getCategories()`, `getCategoryById()` |
| `api/tag.ts` | `getTags()` |
| `api/user.ts` | `getUserInfo()`, `changePassword()` |
| `api/message.ts` | `getMessages()`, `getUnreadCount()`, `markRead()`, `markAllRead()` |
