---
title: 数据流
description: 请求从浏览器到数据库的完整链路 — HTTP → Controller → Service → Repository → DB
domain: architecture
category: data-flow
related_sources:
  - AstarosWiki/src/main/java/com/example/astaroswiki/controller/*.java
  - AstarosWiki/src/main/java/com/example/astaroswiki/service/*.java
  - a-staros-wiki-frontend/src/api/request.ts
  - a-staros-wiki-frontend/src/stores/*.ts
status: final
created: 2026-07-04
updated: 2026-07-04
version: 1.0.0
---

# 数据流

> 所属：`architecture` | 理解请求从浏览器到数据库再返回的完整链路

---

## 请求生命周期

```
浏览器                         Vite Dev Server               Spring Boot                     MySQL
──────                         ──────────────               ────────────                     ─────

1. 用户操作
   ↓
2. Vue Router 匹配路由
   ↓
3. 组件 onMounted()
   ↓
4. api/page.ts
   getPageBySlug(slug)
   ↓
5. Axios 实例
   GET /api/pages/{slug}
   Header: Authorization: Bearer <token>
   │
   └──► 代理转发 ──────────────► 6. JwtAuthenticationFilter
   localhost:3000                    │ 提取 Token → 验证 →
   /api → localhost:8801             │ 注入 SecurityContext
                                     ↓
                                 7. SecurityConfig
                                    匹配 URL 权限规则
                                     ↓
                                 8. PageController
                                    getPageBySlug(slug)
                                     ↓
                                 9. PageService
                                    查 WikiPage by slug
                                    权限检查 (SAVED/DRAFT)
                                    递增 viewCount
                                    convertToDTO()
                                     ↓
                                 10. PageRepository ────────► SELECT * FROM wiki_pages
                                     ↓                          WHERE slug = ?
                                 11. Result<PageDTO> ◄──────── 返回行数据
                                     ↓
   ◄── JSON 响应 ◄────────────── 12. 序列化 JSON
   │
   ▼
13. Axios 响应拦截器
    code==200? → 返回 res.data
    401? → 清除 token, 跳转 /login
    ↓
14. 组件赋值
    page.value = res.data
    ↓
15. Vue 响应式更新 DOM
```

---

## 状态管理数据流

```
Component                    Pinia Store                 API Layer                Backend
─────────                    ───────────                 ─────────                ───────

onMounted()
  │
  └──► store.fetchData()
          │
          ├── loading = true
          │
          └──► api.xxxCall()
                  │
                  └──► service.get(url) ────────────────► HTTP Request
                         │                                   │
                         ◄── response ◄────────────────── JSON Response
                         │
                  code==200?
                  ├── Y → return res.data
                  └── N → ElMessage.error + reject
          │
          ◄── data
          │
          ├── state.value = data
          └── loading = false

  ◄── computed / 直接绑定
  │
  ▼
<template> 自动更新
```

---

## 认证数据流

```
登录:
  Login.vue
    → userStore.login({username, password})
      → api/auth.ts  login(data)
        → POST /api/auth/login  { username, password }
          → AuthService.login()
            → 验证密码 ✓
            → jwtUtil.generateToken(userId, username, role)
            → 返回 { token, username, role, userId }
      ← token 存入 localStorage + Pinia state
    → router.push('/')

后续请求:
  api/request.ts 请求拦截器
    → 从 localStorage 读取 token
    → 注入 Header: Authorization: Bearer <token>

  api/request.ts 响应拦截器
    → 401 → 清除 token → 跳转 /login
    → 403 → ElMessage.error('无权限访问')
    → code≠200 → ElMessage.error + reject
```

---

## 页面审核数据流

```
创作者                      管理员
──────                      ──────

创建页面 / 编辑保存
  POST/PUT /api/pages
  status=SAVED
  draftContent=新内容
        │
        │ [提交审核]
        ▼
  POST/PUT /api/pages
  { publish: true }
  status=DRAFT / draftStatus=DRAFT
        │
        │                       GET /api/admin/requests
        │                       查看待审核列表
        │                            │
        │                       [审核通过] / [打回]
        │                            │
        │                       POST /api/pages/{slug}/approve
        │                       draftContent → content
        │                       status=PUBLISHED
        │                       draftStatus=null
        │                       ◄── 站内消息通知作者
        │
  ◄── 收到审核结果消息
```

---

## 组件通信数据流

```
App.vue
├── Header (nav-link, 搜索, 主题切换)
│   └── 通过 useRouter() 导航
│
├── Sidebar (固定 200px)
│   └── CategoryNav (手风琴树)
│       └── 通过 useCategoryStore() 获取分类
│
├── <router-view>
│   ├── Home.vue
│   │   ├── useCategoryStore().fetchCategories()
│   │   └── usePageStore().fetchRecentPages()
│   │
│   ├── PageDetail.vue
│   │   ├── route.params.slug → getPageBySlug()
│   │   └── useUserStore() 判断编辑权限
│   │
│   └── AdminLayout.vue
│       └── 子路由 (Dashboard, Users, Pages, ...)
│
├── Mailbox (useMessageStore)
└── Footer
```

**规则**：
- 跨组件共享数据 → Pinia Store
- 父子组件通信 → Props + Events
- 路由参数传递 → `route.params` / `route.query`
- 不通过 props 深层传递（prop drilling）
