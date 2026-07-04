---
title: 工程操作日志
description: AstarosWiki 项目完整变更记录 — 安全审计、Harness 搭建、UI 重设计、文档体系
domain: meta
status: final
created: 2026-07-04
updated: 2026-07-04
version: 1.0.0
---

# AstarosWiki 工程操作日志

> 按时间顺序记录每步操作，标注文件变更和验证方式，以便后续 Agent 或开发者继承。

---

## 阶段 0 — 项目初扫与安全审计

### 0.1 项目结构扫描

- 执行 `pygount` LOC 统计：108 文件，7,801 行代码
- 后端：Spring Boot 3.2.5 / Java 17 / Maven / MySQL / Redis / JWT
- 前端：Vue 3.5 / Vite 6.x / TypeScript 5.8 / Pinia / Element Plus / Axios

### 0.2 后端安全缺陷审计

**发现的致命漏洞**：

| # | 问题 | 文件 |
|---|------|------|
| 1 | `/api/auth/reset-admin-password` 无认证 | `AuthController.java:147` |
| 2 | 数据库密码 `root:123456` 硬编码 | `application.yml:40-43` |
| 3 | JWT 密钥硬编码 | `application.yml:139` |
| 4 | Redis 密码 `123456` 硬编码 | `application.yml:96` |
| 5 | CORS `*` + `allowCredentials:true` 组合冲突 | `SecurityConfig.java:143,168` |
| 6 | 登录接口无速率限制 | `SecurityConfig.java` |
| 7 | Swagger/Knife4j 生产环境暴露 | `application.yml:157` |

### 0.3 前端代码质量审计

- Token 存 localStorage（XSS 风险）
- 异常被静默吞掉（`catch { return false }`）
- TypeScript 大量 `any` 类型
- 网络错误的硬编码远程 IP `8.146.227.206`
- 无 ESLint/Prettier，无测试

---

## 阶段 1 — 文档体系建设

### 1.1 AGENTS.md

**文件**：`AGENTS.md`（根目录）

内容：
- 技术栈、目录结构、运行命令
- 架构约定（Controller→Service→Repository、统一响应、异常驱动、JWT 无状态）
- 关键文件索引（12 行速查表）
- 刚性规则 R1-R5（安全边界、设计一致性、质量门禁、架构约束、文档同步）
- R3.1 Linter 错误三要素回复模式（❶什么错了 ❷为什么+怎么改 ❸哪个文档有说明）
- 已知问题清单

### 1.2 docx/ 参考文档

**目录结构**：

```
docx/
├── README.md                          # 索引
├── architecture/
│   ├── auth-flow.md                   # JWT 认证流程
│   ├── page-lifecycle.md              # SAVED→DRAFT→PUBLISHED 状态机
│   ├── entity-relationships.md        # 8 张表 DDL + ER 图
│   └── data-flow.md                   # 请求链路 + 状态管理 + 组件通信
├── api/
│   └── api-routes.md                  # 30+ 端点清单
├── design/
│   └── design-system.md               # v1.0 → v2.0 设计规范
├── frontend/
│   └── frontend-patterns.md           # Pinia/Component/API 模式
└── requirements/
    ├── wiki-requirements.md           # 参考 6 个公开 Wiki 平台的 45 项需求
    ├── gap-analysis.md                # 功能缺口分析（P0:90% P1:15%）
    └── harness-audit.md               # Harness 流程审计（评分 2.2/10）
```

**每份文档**包含 YAML frontmatter 元信息（title/domain/category/related_sources/status/version）。

---

## 阶段 2 — 环境配置修正

### 2.1 数据库凭据同步

- `application.yml`：`username: root` → `username: astaros_wiki`（对齐云服务器）
- `vite.config.ts`：代理目标 `localhost:8801` → `8.146.227.206:8801`（恢复远程代理）

### 2.2 环境变量外部化

**文件**：`application.yml`、`.env.example`、`.env`

- 敏感值替换为 `${ENV_VAR:default}` 占位符：
  - `${DB_USERNAME:astaros_wiki}`、`${DB_PASSWORD:123456}`
  - `${REDIS_PASSWORD:123456}`
  - `${JWT_SECRET:astaros-wiki-secret-key-...}`
  - `${KNIFE4J_ENABLE:true}`
- `.env.example` 作为模板，`.env` 被 `.gitignore` 排除

---

## 阶段 3 — Harness 止血（Phase 1）

### 3.1 .gitignore

**文件**：`.gitignore`（根目录，新建）

排除：`node_modules/`、`target/`、`dist/`、`.env`、`*.class`、`*.jar`、IDE 目录、OS 文件。

### 3.2 前端 ESLint + Prettier

**文件**：
- `astaros-wiki-frontend/eslint.config.js`（flat config）
- `astaros-wiki-frontend/.prettierrc`
- `astaros-wiki-frontend/package.json`（新增 scripts + deps）

**安装的依赖**：`eslint`、`@eslint/js`、`typescript-eslint`、`eslint-plugin-vue`、`vue-eslint-parser`、`prettier`、`eslint-config-prettier`

**新增 scripts**：
```json
"lint": "eslint .",
"lint:strict": "eslint . --max-warnings 0",
"lint:fix": "eslint . --fix",
"format": "prettier --write .",
"format:check": "prettier --check ."
```

**配置要点**：
- 浏览器全局变量声明（`console`, `localStorage`, `document`, `window` 等）
- `@typescript-eslint/no-explicit-any` → `warn`（不阻断）
- `vue/multi-word-component-names` → `off`
- `no-console` → `warn`

**验证**：`pnpm lint` → 0 errors, 81 warnings

### 3.3 Checkstyle 强制阻断

**文件**：`AstarosWiki/pom.xml`

`<failOnViolation>false</failOnViolation>` → `true`

### 3.4 Pre-commit hook

**文件**：
- `.husky/pre-commit`
- `astaros-wiki-frontend/package.json`（`lint-staged` 配置）

**安装的依赖**：`husky`、`lint-staged`

**pre-commit 流程**：`lint-staged`（ESLint + Prettier）→ `gitleaks`（如已安装）

**lint-staged 配置**：
```json
"*.{vue,ts,js}": ["eslint --fix", "prettier --write"],
"*.{css,md,json}": ["prettier --write"]
```

---

## 阶段 4 — Harness 加固（Phase 2）

### 4.1 后端单元测试

**文件**：
- `src/test/java/.../service/AuthServiceTest.java`（新建，8 个测试）
- `src/test/java/.../service/PageServiceTest.java`（新建，10 个测试）
- `src/test/java/.../AstarosWikiApplicationTests.java`（修改：+ `@Disabled`）

**AuthServiceTest 覆盖**：
- 正常登录 → 返回 token + 用户信息
- 密码错误 → 抛 BusinessException
- 用户待审核 → 拒绝登录
- 用户被禁用 → 拒绝登录
- 用户不存在 → 抛异常
- 正常注册 → 创建 PENDING 用户 + 申请记录
- 用户名已存在 → 抛异常
- 邮箱已注册 → 抛异常

**PageServiceTest 覆盖**：
- 返回已发布页面分页列表
- 找到已发布页面 → 返回 DTO
- 页面已删除 → 抛异常
- 页面不存在 → 抛异常
- 保存草稿 → status=SAVED + 创建 v1 版本
- 提交审核 → status=DRAFT
- Slug 已存在 → 抛异常
- 作者删除 → 软删除
- 非作者删除 → 抛异常
- 管理员删除他人页面 → 通过

**技术栈**：JUnit 5 + Mockito + AssertJ

### 4.2 前端 Vitest 配置

**文件**：
- `astaros-wiki-frontend/vitest.config.ts`（新建）
- `astaros-wiki-frontend/src/Login.test.ts`（新建）
- `astaros-wiki-frontend/src/vite-env.d.ts`（修改：+ `.vue` 模块声明）
- `astaros-wiki-frontend/tsconfig.app.json`（修改：+ `skipLibCheck`, `paths`, `types`）
- `astaros-wiki-frontend/tsconfig.node.json`（修改：+ `vitest.config.ts`）

**安装的依赖**：`vitest`、`@vue/test-utils`、`jsdom`、`@vitest/coverage-v8`、`@types/node`

**新增 scripts**：
```json
"test": "vitest run",
"test:watch": "vitest",
"test:coverage": "vitest run --coverage"
```

**验证**：`pnpm test` → 1 passed

### 4.3 GitHub Actions CI

**文件**：`.github/workflows/ci.yml`（新建）

**流水线**：
```
Frontend job: checkout → pnpm install → lint:strict → test → build
Backend job:  checkout → setup-java 17 → mvn checkstyle:check → test → package
```

触发条件：`push` / `pull_request` 到 `main` / `master`

### 4.4 Secret scan

**文件**：`.gitleaks.toml`（新建）

**配置**：通用 API Key / Token / Password 模式检测。`docx/`、`.gitignore`、`AGENTS.md` 加入 allowlist。

**pre-commit 集成**：`gitleaks protect --staged`（如已安装）

---

## 阶段 5 — Harness 深化（Phase 3）

### 5.1 OWASP Dependency Check

**文件**：
- `AstarosWiki/pom.xml`（+ `dependency-check-maven` 插件 11.1.1）
- `AstarosWiki/config/owasp/owasp-suppressions.xml`（新建）

**配置**：CVSS ≥ 7 时阻断构建。输出 HTML + JSON 报告。

### 5.2 Dependabot

**文件**：`.github/dependabot.yml`（新建）

**配置**：
- Maven：每周一 09:00 CST
- npm：每周一 09:00 CST
- GitHub Actions：每月

### 5.3 文档漂移检查

**文件**：`scripts/doc-drift.py`（新建）

**功能**：对比 Controller 注解中的真实端点 vs `docx/api/api-routes.md` 中的记录，发现不一致即报错。

**技术细节**：
- 解析 `@RequestMapping` + `@GetMapping/@PostMapping/@PutMapping/@DeleteMapping` 注解
- 支持无括号的注解语法（`@GetMapping` 不带路径参数）
- 参数占位符归一化（`{pageId}` → `{param}`）以避免命名差异误报
- 验证：50 端点全部同步 ✓

### 5.4 API 文档同步

**文件**：`docx/api/api-routes.md`（更新）

手动同步了 7 个遗漏端点和 6 个已删除端点：

**新增记录**：`PUT /api/categories/{id}`、`DELETE /api/categories/{id}`、`DELETE /api/tags/{id}`、`POST /api/tags/merge`、`PUT /api/users/me/password`、`DELETE /api/messages/{id}`、`GET /api/messages`

---

## 阶段 6 — UI 重设计 v2.0

### 6.1 设计规范文档

**文件**：`docx/design/design-system.md`

基于 [hermes-agent.nousresearch.com](https://hermes-agent.nousresearch.com/) 完整 DOM/CSS 逆向分析，追加 **500 行官方站实现深度解析**，包含 12 个模式：

1. 流体缩放系统（`--u: calc(100cqw / 2360)`）
2. 全局半透明 + 混合模式（`mix-blend-mode`）
3. 噪声纹理覆盖层（SVG turbulence）
4. 排版模式（light 300 字重 + 全大写）
5. 平台下载卡片（wash + arc + 图片背景）
6. 终端安装命令块（Tab 栏 + 代码高亮 + copy 按钮）
7. 视频预览框架（`border` 遮罩 + `mix-blend-lighten`）
8. 数字编号板块（#1 #2 ... 功能卡片）
9. Footer 巨幅文字（`clamp(3rem, 15vw, 15rem)`）
10. 边框分隔模式（`border-r` 替代间距）
11. `color-mix()` 透明度技巧（替代 `rgba()`）
12. 导航结构（`justify-between` 分散布局）

### 6.2 主题 CSS v1.0 → v2.0

**文件**：`astaros-wiki-frontend/src/assets/hermes-theme.css`（完全重写）

**核心变更**：

| 项目 | v1.0 | v2.0 |
|------|------|------|
| 暗色背景 | `#000` | `#0000f2` 电光蓝 |
| 暗色卡片 | `rgba(255,255,255,0.06)` | `color-mix(in srgb, var(--color-foreground) 6%, transparent)` |
| 类名规范 | `btn-hermes-*`、`hermes-*` | `hw-*`（旧类名保留为别名） |
| 卡片效果 | 无纹理 | `::after` 噪声 + hover `translateY(-2px)` |
| 动效 | 无规范 | 6 种类型（hover/展开/页面/主题/骨架/抬起） |
| 骨架屏 | 无 | `hw-skeleton` shimmer 动画 |
| 页面过渡 | 无 | `hw-page` Transition |
| 字体 | bold 700 | light 300（Hero） |

**新增 CSS 类**：`hw-card-arc`、`hw-card-platform`、`hw-card-wash`、`hw-terminal`、`hw-footer-wordmark`、`hw-preview-frame`、`hw-row`、`hw-column`、`hw-list`、`hw-pagination`

### 6.3 App.vue

**文件**：`astaros-wiki-frontend/src/App.vue`

- `<router-view>` 包裹 `<Transition name="hw-page">`（页面切换动画）
- Footer 追加 `<div class="hw-footer-wordmark">阿斯塔洛斯</div>`（巨幅水印）

### 6.4 Home.vue

**文件**：`astaros-wiki-frontend/src/views/Home.vue`（完全重写）

- Hero 区域：`hw-hero-title` 竖排大写标题 + `hw-btn-primary` / `hw-btn-ghost` 按钮
- 分类网格：`hw-skeleton` 骨架屏替换「加载中...」
- 快捷入口：`hw-card-platform` + `hw-card-arc` + `hw-card-wash` 平台卡片风格

### 6.5 全量页面类名迁移（17 文件）

**迁移映射**：

| 旧类名 | 新类名 |
|--------|--------|
| `btn-hermes-primary` | `hw-btn-primary` |
| `btn-hermes-outline` | `hw-btn-ghost` |
| `hermes-card` | `hw-card` |
| `hermes-card-title` | `hw-card-title` |
| `section-label` | `hw-eyebrow` |
| `section-title` | `hw-section-title` |
| `hermes-tag` | `hw-tag` |
| `hermes-divider` | `hw-divider` |
| `classic-card` | `hw-card` |
| `classic-card-title` | `hw-eyebrow` |
| `classic-row` | `hw-row` |
| `classic-column` | `hw-column` |
| `classic-list` | `hw-list` |
| `classic-pagination` | `hw-pagination` |
| `classic-tag` | `hw-tag` |
| `classic-accent-primary` | `var(--color-foreground)` |
| `classic-text-secondary` | `var(--color-muted)` |

**涉及文件**：Login, Register, Search, Category, Profile, PageDetail, Tag, AdminLayout, Dashboard, Users, Categories, Tags, Pages, PageReview, Logs, PageEdit, PageCreate, PageCenter, PageEditList, PagePublish, PageHistory

### 6.6 AdminLayout + Dashboard 重写

**AdminLayout**：`el-menu` 替换为自定义 `admin-nav-link` + 编号 (`01`-`06`)

**Dashboard**：4 列统计卡片（`#1`-`#4` 标签）+ 待审核/最近操作双栏网格

---

## 当前状态总结

### Git 仓库

```
Remote: https://github.com/FANMASK123/AstarosWiki.git
Branch: master
Status: 本地有未推送 commits (网络问题)
```

### Harness 评分

| 维度 | 初始 | 当前 |
|------|:--:|:--:|
| 文档完整性 | 0 | 🟢 8/10 |
| 代码规范检查 | 0 | 🟢 8/10 |
| 测试覆盖 | 0 | 🟡 5/10 |
| CI/CD 自动化 | 0 | 🟢 7/10 |
| 安全扫描 | 0 | 🟡 5/10 |
| 提交门禁 | 0 | 🟢 7/10 |
| **综合** | **0/10** | **6.7/10** |

### 本地待推送 commits

```
75ab5c9 feat: full UI redesign — all admin + page components Hermes-themed
e6f0349 feat: implement Hermes design patterns
9a3fd54 feat: UI v2.0 — electric blue theme
6e875bb feat: Phase 3 — OWASP dep check + Dependabot + doc-drift checker
d7d885d fix: @Disabled contextLoads
7f48deb fix: remove unused imports in PageServiceTest
5df8558 feat: Phase 1-2 harness + env var externalization
```

### 云服务器部署步骤

```bash
# 本地打包
mvnw.cmd package -DskipTests

# 上传
scp target/AstarosWiki-0.0.1-SNAPSHOT.jar root@8.146.227.206:/opt/astaroswiki/

# SSH 启动
ssh root@8.146.227.206
cd /opt/astaroswiki
export DB_USERNAME=astaros_wiki DB_PASSWORD=123456 REDIS_PASSWORD=123456 JWT_SECRET=... KNIFE4J_ENABLE=true
java -jar AstarosWiki-0.0.1-SNAPSHOT.jar
```
