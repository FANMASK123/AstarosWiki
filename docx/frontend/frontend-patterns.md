---
title: 前端开发模式
description: Vue 3 开发规范 — Pinia Store、Vue Router、API 层、组件模式
domain: frontend
category: conventions
related_sources:
  - a-staros-wiki-frontend/src/api/request.ts
  - a-staros-wiki-frontend/src/stores/user.ts
  - a-staros-wiki-frontend/src/router/index.ts
  - a-staros-wiki-frontend/src/views/Home.vue
  - a-staros-wiki-frontend/src/views/Login.vue
  - a-staros-wiki-frontend/vite.config.ts
  - a-staros-wiki-frontend/package.json
status: final
created: 2026-07-04
updated: 2026-07-04
version: 1.0.0
---

# 前端开发模式

> 所属：`frontend` | 关联源码：`src/api/`、`src/stores/`、`src/router/`、`src/views/`

---

## 项目配置

| 依赖 | 版本 |
|------|------|
| Vue | `^3.5.13` |
| Vite | `^6.3.5` |
| TypeScript | `~5.8.3` |
| Vue Router | `^4.4.0` |
| Pinia | `^2.1.7` |
| Axios | `^1.7.2` |
| Element Plus | `^2.7.6` |

### Vite 配置

```typescript
// vite.config.ts
server: {
  port: 3000,
  proxy: { '/api': { target: 'http://localhost:8801', changeOrigin: true } }
}
```

---

## 目录约定

```
src/
├── api/            # Axios 请求层（按域拆分）
│   ├── request.ts  #   实例 + 拦截器 + 封装方法
│   ├── auth.ts     #   login(), register()
│   ├── page.ts     #   页面 CRUD
│   ├── admin.ts    #   管理功能
│   ├── category.ts #   分类
│   ├── tag.ts      #   标签
│   ├── user.ts     #   用户
│   └── message.ts  #   消息
│
├── stores/         # Pinia 状态管理
│   ├── user.ts     #   用户状态 + 登录/登出
│   ├── page.ts     #   页面列表
│   ├── category.ts #   分类树
│   └── tag.ts      #   标签列表
│
├── router/
│   └── index.ts    # 路由定义 + beforeEach 守卫
│
├── views/          # 页面组件
│   ├── Home.vue
│   ├── Login.vue, Register.vue, Search.vue
│   ├── Category.vue, Tag.vue, Profile.vue
│   ├── page/       #   Wiki 页面子页面
│   └── admin/      #   管理后台子页面
│
├── components/     # 通用组件
│   ├── Sidebar.vue
│   ├── CategoryNav.vue
│   ├── Mailbox.vue
│   └── ...
│
└── assets/
    └── hermes-theme.css  # 全局设计系统
```

---

## Pinia Store 模式

```typescript
// stores/user.ts
export const useUserStore = defineStore('user', () => {
  // 1. State — 从 localStorage 恢复初始值
  const token = ref<string | null>(localStorage.getItem('token'))
  const userInfo = ref<AuthResponse | null>(
    localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')!) : null
  )

  // 2. Actions — 异步操作
  const login = async (data: LoginRequest) => {
    try {
      const res = await loginApi(data)
      if (res.code === 200) {
        token.value = res.data.token
        userInfo.value = res.data
        localStorage.setItem('token', res.data.token)
        localStorage.setItem('user', JSON.stringify(res.data))
        ElMessage.success('登录成功')
        return true
      }
      return false
    } catch {
      return false
    }
  }

  const logout = () => {
    token.value = null
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  // 3. Getters — 派生状态
  const isLoggedIn = () => token.value && userInfo.value
  const isAdmin = () => userInfo.value?.role === 'ADMIN'

  return { token, userInfo, login, logout, isLoggedIn, isAdmin }
})
```

### 使用

```typescript
import { useUserStore } from '@/stores/user'
const userStore = useUserStore()
userStore.login({ username, password })
```

---

## Vue Router 路由守卫

```typescript
// router/index.ts
router.beforeEach((to, _from, next) => {
  const userStore = useUserStore()

  if (to.meta.requiresAuth) {
    if (!userStore.isLoggedIn()) return next('/login')
    if (to.meta.requiresAdmin && !userStore.isAdmin()) return next('/')
  }
  next()
})
```

### 路由 Meta

| Meta | 效果 |
|------|------|
| `requiresAuth: true` | 未登录 → 跳转 `/login` |
| `requiresAdmin: true` | 非管理员 → 跳转 `/` |

---

## 组件模式

```vue
<template>
  <div class="page-wrapper">
    <p class="section-label">标签</p>
    <h1 class="section-title">标题</h1>
    <!-- 内容 -->
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useXxxStore } from '../stores/xxx'

const route = useRoute()
const router = useRouter()
const store = useXxxStore()

const data = ref<Type | null>(null)

onMounted(async () => {
  try {
    const res = await apiCall()
    if (res.code === 200) data.value = res.data
  } catch (error) {
    console.error('...', error)
  }
})
</script>

<style scoped>
/* 组件私有样式 — 使用 CSS 变量 */
.page-wrapper {
  background: var(--color-background);
  color: var(--color-foreground);
}
</style>
```

### 规范

| 规则 | 说明 |
|------|------|
| 组合式 API | `<script setup lang="ts">` |
| 局部样式 | `<style scoped>` |
| 图标导入 | `import { Search } from '@element-plus/icons-vue'` |
| CSS 颜色 | 必须用 `var(--color-xxx)` |
| 异步操作 | `async/await` + `try/catch` |

---

## API 调用模式

```typescript
// 组件中调用
import { getPageBySlug } from '../../api/page'

const res = await getPageBySlug(slug)
if (res.code === 200) {
  page.value = res.data
}
```

### 反模式（禁止）

```typescript
// ❌ 组件中直接调 axios
import axios from 'axios'
const res = await axios.get('/api/pages/' + slug)

// ❌ catch 块空吞异常
try { ... } catch (error) {}

// ❌ 不校验 code 直接读 data
const res = await apiCall()
page.value = res.data  // 可能 res.code !== 200

// ❌ 未清理的 import（lint 报 TS6133）
import type { CategoryDTO } from '../api/category'  // 重构后不再使用却未删除
```

### Linter 错误三要素回复模式

每次构建报错时，必须按以下三段回复。缺一不可：

| 要素 | 内容 | 格式 |
|:--:|------|------|
| ❶ | 什么错了 | 原始错误信息（文件:行号 + 错误码 + 消息） |
| ❷ | 为什么错 + 怎么改 | 一句话根因 + before/after 代码片段 |
| ❸ | 哪个文档有说明 | `docx/` 路径链接 → 本地可查 |

**示例**：

```
❶ [什么错了]
src/components/CategoryNav.vue(102,1): error TS6133:
'CategoryDTO' is declared but its value is never read.

❷ [为什么错了，怎么改]
重构后状态管理从 CategoryDTO|null 改为 Set<number>，
旧的类型导入变成了死代码。

// before
import type { CategoryDTO } from '../api/category'
                            // ← TS6133: declared but never read

// after
// 删除该行即可

❸ [哪个文档有详细说明]
docx/frontend/frontend-patterns.md § 反模式：未清理的 import
```

---

## 构建命令

| 命令 | 作用 |
|------|------|
| `pnpm dev` | Vite 开发服务器（port 3000） |
| `pnpm build` | `vue-tsc -b && vite build` → `dist/` |
| `pnpm preview` | 预览生产构建 |

构建输出：
```
dist/
├── index.html
└── assets/
    ├── index-xxx.js     ← 主 chunk
    ├── Home-xxx.js      ← 路由级代码分割
    ├── Login-xxx.js
    └── index-xxx.css    ← 全局 CSS + Element Plus
```
