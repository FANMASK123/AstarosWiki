# AGENTS.md — AstarosWiki

> AI 编码助手的结构化上下文地图。渐进式披露：先读完本节，再按需深入 `docx/` 参考文档。

## 项目概述

阿斯塔洛斯 Wiki 知识库系统 — 前后端分离，支持页面 CRUD、分类/标签、版本历史、草稿审核、站内消息、管理员后台。

## 技术栈

| 层 | 技术 | 版本 |
|---|------|------|
| 后端 | Spring Boot + JPA + Security + Maven | 3.2.5 / Java 17 |
| 数据库 | MySQL + Redis | — |
| 认证 | JWT (jjwt) + BCrypt | 0.12.5 |
| 文档 | Knife4j (Swagger) | 4.5.0 |
| 前端 | Vue 3 + Vite + TypeScript | 3.5 / 6.x / 5.8 |
| 状态 | Pinia | 2.1 |
| 路由 | Vue Router | 4.4 |
| UI | Element Plus | 2.7 |

## 目录结构

```
AstarosWiki/
├── AGENTS.md                     # ← 你在这里
├── docx/                         # 📚 参考文档（详情见 docx/README.md）
│   ├── README.md                 #    文档索引
│   ├── architecture/             #    系统架构
│   │   ├── auth-flow.md          #      认证流程
│   │   ├── page-lifecycle.md     #      页面生命周期
│   │   ├── entity-relationships.md #   实体关系
│   │   └── data-flow.md          #      数据流
│   ├── requirements/             #    需求规格
│   │   ├── wiki-requirements.md  #      Wiki 设计需求
│   │   └── gap-analysis.md       #      功能缺口分析
│   ├── api/                      #    API 参考
│   │   └── api-routes.md         #      路由表
│   ├── design/                   #    设计规范
│   │   └── design-system.md      #      Hermes 主题
│   └── frontend/                 #    前端规范
│       └── frontend-patterns.md  #      开发模式
│
├── AstarosWiki/                  # 🔧 Spring Boot 后端
│   └── src/main/java/com/example/astaroswiki/
│       ├── config/               # SecurityConfig
│       ├── controller/           # Auth, Page, Admin, User, Tag, Category, Message
│       ├── service/              # Auth, Page, Admin, Category, Tag, Message
│       ├── entity/               # User, WikiPage, PageVersion, Category, Tag, Message, …
│       ├── dto/                  # 按域分包: auth/, page/, tag/, category/, message/
│       ├── repository/           # JPA Repository 接口
│       ├── security/             # JwtAuthenticationFilter, CustomUserDetailsService
│       ├── util/                 # JwtUtil, SecurityUtil, PasswordHashGenerator
│       ├── enums/                # PageStatus, UserRole, UserStatus, RequestStatus, RequestType
│       ├── exception/            # BusinessException, GlobalExceptionHandler
│       └── common/               # Result<T>, PageResult<T>
│   └── src/main/resources/
│       ├── application.yml       # 开发配置（⚠️ 含硬编码密码）
│       └── application-prod.yml  # 生产配置占位
│
├── astaros-wiki-frontend/        # 🎨 Vue 3 前端
│   └── src/
│       ├── api/                  # Axios 请求层: request.ts, auth, page, user, …
│       ├── stores/               # Pinia: user, category, tag, page
│       ├── router/               # Vue Router 配置
│       ├── views/                # 页面组件（按域分包: admin/, page/）
│       ├── components/           # 通用组件: Sidebar, CategoryNav, Mailbox, …
│       └── assets/               # hermes-theme.css (设计系统)
│
├── CODE_REVIEW_REPORT.md
└── LEARNING_GUIDE.md
```

## 运行命令

```bash
# 后端（端口 8801）
cd AstarosWiki
mvn spring-boot:run

# 前端（端口 3000，代理到 localhost:8801）
cd astaros-wiki-frontend
pnpm dev

# 前端构建
pnpm run build      # = vue-tsc -b && vite build
```

## 架构约定

### 后端

- **分层**: Controller → Service → Repository（Controller 不处理业务逻辑）
- **响应**: 全部包裹 `Result<T>` — `Result.success(data)` / `Result.error(msg)`
- **异常**: 业务异常抛 `BusinessException`，由 `GlobalExceptionHandler` 统一转为 JSON
- **认证**: 无状态 JWT。`JwtAuthenticationFilter` 在 `UsernamePasswordAuthenticationFilter` 之前执行。角色前缀 `ROLE_`
- **权限**: `SecurityConfig` 定义 URL 规则 + `@PreAuthorize` 方法级注解
- **实体**: Lombok `@Data`，`@PrePersist` 自动设时间戳
- **页面生命周期**: SAVED → DRAFT → PUBLISHED。已发布页面草稿存 `draftContent`，审核通过后覆盖主 `content`
- **软删除**: `status = DELETED`，管理员可恢复或永久删除

### 前端

- **API 层**: `src/api/request.ts` 封装 Axios 实例（baseURL `/api`，拦截器自动注入 Bearer token，401 → 清除登录态跳转）
- **状态**: Pinia stores 按域拆分，初始化时从 localStorage 恢复 token/user
- **路由守卫**: `router.beforeEach` 校验 `meta.requiresAuth` / `meta.requiresAdmin`
- **设计系统**: 所有颜色使用 CSS 变量，详见 [`docx/design/design-system.md`](docx/design/design-system.md)

## 关键文件索引

| 想了解… | 源码入口 | 深度参考 |
|---------|---------|---------|
| 设计规范 | [`requirements/wiki-requirements.md`](docx/requirements/wiki-requirements.md) |
| 功能缺口 | [`requirements/gap-analysis.md`](docx/requirements/gap-analysis.md) |
| 认证流程 (JWT/权限/过滤器) | `config/SecurityConfig.java` | [`architecture/auth-flow.md`](docx/architecture/auth-flow.md) |
| 页面生命周期 (状态机/审核) | `service/PageService.java` | [`architecture/page-lifecycle.md`](docx/architecture/page-lifecycle.md) |
| 数据库表结构 (ER/DDL/JPA) | `entity/*.java` | [`architecture/entity-relationships.md`](docx/architecture/entity-relationships.md) |
| 数据流 (请求链路/状态管理) | — | [`architecture/data-flow.md`](docx/architecture/data-flow.md) |
| API 路由表 (全部端点) | `controller/*.java` | [`api/api-routes.md`](docx/api/api-routes.md) |
| 设计系统 (CSS 变量/组件类) | `assets/hermes-theme.css` | [`design/design-system.md`](docx/design/design-system.md) |
| 前端模式 (Store/Router/API) | `stores/`, `router/`, `api/` | [`frontend/frontend-patterns.md`](docx/frontend/frontend-patterns.md) |
| 用户状态 | `stores/user.ts` | [`frontend-patterns.md`](docx/frontend/frontend-patterns.md) § Store 模式 |

---

# 刚性需求与规则

> 以下规则具有最高优先级。违反任一条的代码将被拒绝。

## R1 — 安全边界（不可逾越）

- **禁止修改** `SecurityConfig.java` 的权限规则，除非用户明确授权
- **禁止修改** 认证端点的访问控制，除非用户明确授权
- **禁止硬编码** 新的密码、密钥、Token。敏感值必须通过环境变量注入
- **禁止新增** 无认证的敏感操作端点
- **禁止引入** `v-html` / `innerHTML` 渲染未消毒的用户输入（XSS 防护）

## R2 — 设计一致性（不可偏离）

- **必须使用** CSS 变量（`var(--color-*)`），禁止新的颜色硬编码
- **所有按钮** → `btn-hermes-primary` 或 `btn-hermes-outline`
- **所有卡片** → `hermes-card`
- **新页面布局** 参考 `Home.vue` / `Login.vue` 作为模板
- **暗色/亮色** 不写独立样式，依赖 `[data-theme]` 自动切换

## R3 — 代码质量门禁（不可跳过）

- **每次改动后必须运行** `pnpm run build`，零错误才通过
- **TypeScript** 不得引入 `any` 类型的新导出函数签名
- **不得静默吞异常** — 所有 `catch` 块必须至少 `console.error`
- **API 响应** 必须校验 `res.code === 200` 再读 `res.data`
- **Linter 报错必须用三要素模式回复**（见下方 R3.1）

### R3.1 — Linter 错误三要素回复模式

每次 `pnpm run build` / `vue-tsc` 报错时，回复必须包含三段，缺一不可：

```
❶ [什么错了]
   └── 粘贴原始错误信息（文件:行号 + 错误码 + 消息）

❷ [为什么错了，怎么改]
   └── 一句话解释根因
   └── 附 before/after 代码片段（完整可读，非 diff patch）

❸ [哪个文档有详细说明]
   └── docx/ 下的具体 md 文件链接
   └── 如为新问题 → 同步更新该文档
```

**示例**：

```
❶ [什么错了]
src/components/CategoryNav.vue(102,1): error TS6133:
'CategoryDTO' is declared but its value is never read.

❷ [为什么错了，怎么改]
未使用的 import 导致 TypeScript 编译失败。
删除该行即可 — CategoryNav 已改用 Set<number> 管理展开状态，
不再需要 CategoryDTO 类型。

// before
import type { CategoryDTO } from '../api/category'

// after — 删除该行
import { FolderOpened, Folder } from '@element-plus/icons-vue'

❸ [哪个文档有详细说明]
docx/frontend/frontend-patterns.md § 组件模式 — 反模式：未清理的 import
```

## R4 — 架构约束（不可破坏）

- **后端**: Controller 不写业务逻辑 → Service 层
- **前端**: 组件不直接调 `axios` → 走 `src/api/` 层
- **状态**: 跨组件共享数据 → Pinia Store，不深层传递 props
- **不引入新范式**: 新库/新框架/新构建工具 → 需用户批准

## R5 — 文档同步（不可滞后）

| 变更 | 更新文件 |
|------|---------|
| 新增 API 端点 | [`docx/api/api-routes.md`](docx/api/api-routes.md) |
| 新增实体/字段 | [`docx/architecture/entity-relationships.md`](docx/architecture/entity-relationships.md) |
| 状态机变更 | [`docx/architecture/page-lifecycle.md`](docx/architecture/page-lifecycle.md) |
| CSS 变量/类变更 | [`docx/design/design-system.md`](docx/design/design-system.md) |
| 前端模式变更 | [`docx/frontend/frontend-patterns.md`](docx/frontend/frontend-patterns.md) |

---

## 设计系统（Hermes 主题）

```css
/* 核心变量（暗色默认） */
--color-background: #000;
--color-foreground: #f8fafc;
--color-card: #060606;
--color-border: #f8fafc21;
--color-muted-foreground: #8f98a8;

/* 可用类 */
.btn-hermes-primary   /* 深色实心按钮 */
.btn-hermes-outline   /* 边框轮廓按钮 */
.hermes-card          /* 卡片容器 */
.nav-link             /* 导航链接 */
.section-label        /* 区域标签 */
.section-title        /* 区域标题 */
.hermes-tag           /* 标签 */
.hermes-divider       /* 分割线 */
```

详见 [`docx/design/design-system.md`](docx/design/design-system.md)。

## 已知问题（禁止重复报告）

1. **`/api/auth/reset-admin-password` 无认证** — 任何人可重置管理员密码为 `admin123`
2. **application.yml 含硬编码密码** — DB/Redis/JWT 密钥明文，生产必须改为环境变量
3. **CORS `*` + `allowCredentials: true`** — 浏览器会拒绝此组合
4. **无速率限制** — 登录/注册接口可被暴力破解
5. **前端 Token 存 localStorage** — XSS 风险
6. **Vite proxy 目标** — 本地用 `localhost:8801`，远程部署需改

## 工作原则

- **基于现有代码迭代** — 先读懂已有模式再改
- **后端不改安全配置** — 除非用户明确要求
- **前端遵循 Hermes 设计系统** — 使用已有 CSS 变量/类
- **构建即验证** — 改动后跑 `pnpm run build`
- **编辑用 patch** — 小改动优先 `patch`，大文件重写用 `write_file`
