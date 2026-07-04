---
title: Wiki 系统设计需求
description: 参考 Wikipedia、Confluence、Notion、Wiki.js、Obsidian 等公开 Wiki 平台提炼的功能需求规格
domain: requirements
category: product-spec
references:
  - https://www.mediawiki.org/         # Wikipedia 引擎
  - https://www.confluence.com/        # Atlassian Confluence
  - https://www.notion.so/             # Notion
  - https://js.wiki/                   # Wiki.js
  - https://obsidian.md/               # Obsidian
status: final
created: 2026-07-04
updated: 2026-07-04
version: 1.0.0
---

# Wiki 系统设计需求

> 参考 Wikipedia (MediaWiki)、Confluence、Notion、Wiki.js、Obsidian 等公开 Wiki 平台提炼的功能需求规格。

---

## RQ1 — 内容创作

### RQ1.1 编辑器

| 需求 | 优先级 | 说明 | 参考 |
|------|:--:|------|------|
| Markdown 编辑器 | 🔴 P0 | 支持 Markdown 语法，所见即所得或分屏预览 | Notion, Wiki.js, Obsidian |
| 富文本工具栏 | 🟠 P1 | 加粗/斜体/标题/列表/引用/链接/图片等快捷按钮 | Confluence, MediaWiki |
| 实时预览 | 🟠 P1 | 编辑时实时渲染预览，分屏或切换模式 | Obsidian, Wiki.js |
| 图片粘贴上传 | 🟠 P1 | Ctrl+V 粘贴剪贴板图片自动上传 | Notion, Confluence |
| 内部链接自动补全 | 🟡 P2 | 输入 `[[` 弹出已有页面列表供选择 | Obsidian, Notion |
| 拖拽排序段落 | 🔵 P3 | 拖拽移动内容块 | Notion |

### RQ1.2 页面结构

| 需求 | 优先级 | 说明 |
|------|:--:|------|
| 自动生成目录 (TOC) | 🟠 P1 | 根据标题层级自动生成锚点目录 |
| 面包屑导航 | 🟡 P2 | 首页 > 分类 > 子分类 > 当前页面 |
| 页面模板 | 🟡 P2 | 创建页面时可选择预定义模板 |
| 自定义 Slug | 🟡 P2 | 支持自定义 SEO 友好 URL |
| 页面元信息 | 🟡 P2 | 作者/创建时间/修改时间/阅读时长 |

---

## RQ2 — 版本控制

| 需求 | 优先级 | 说明 | 参考 |
|------|:--:|------|------|
| 版本历史列表 | 🔴 P0 | 每个页面保留所有历史版本 | MediaWiki, Confluence |
| 版本差异对比 | 🔴 P0 | 任意两个版本的内容 diff（并排或统一视图） | MediaWiki, GitHub Wiki |
| 回滚到指定版本 | 🟠 P1 | 一键恢复到历史版本 | MediaWiki |
| 编辑摘要 | 🟡 P2 | 每次编辑填写变更说明 | MediaWiki |
| 版本作者/时间线 | 🟡 P2 | 展示每个版本的编辑者和时间 | MediaWiki |

---

## RQ3 — 协作与审核

| 需求 | 优先级 | 说明 | 参考 |
|------|:--:|------|------|
| 页面评论/讨论 | 🟠 P1 | 页面底部评论区域，支持回复 | Confluence, Notion |
| @提及用户 | 🟡 P2 | 评论中 @用户名发送通知 | Confluence, Notion |
| 审核工作流 | 🟠 P1 | 草稿 → 提交审核 → 通过/打回 | MediaWiki (FlaggedRevs) |
| 页面锁定/保护 | 🟡 P2 | 防止编辑冲突或保护重要页面 | MediaWiki |
| 编辑冲突检测 | 🔵 P3 | 多人同时编辑时提示冲突 | MediaWiki |

---

## RQ4 — 内容组织

| 需求 | 优先级 | 说明 | 参考 |
|------|:--:|------|------|
| 分类树（多级） | 🔴 P0 | 支持无限层级分类 | MediaWiki, Confluence |
| 标签系统 | 🟠 P1 | 扁平标签，支持多标签 | Notion, Wiki.js |
| 双向链接 (Backlinks) | 🟠 P1 | 显示"哪些页面链接到了本页" | Obsidian, Notion |
| 关联页面推荐 | 🟡 P2 | 根据分类/标签自动推荐相关页面 | Confluence |
| 页面树/侧边栏导航 | 🟠 P1 | 按分类层级的可折叠树形导航 | Confluence, Wiki.js |

---

## RQ5 — 搜索与发现

| 需求 | 优先级 | 说明 | 参考 |
|------|:--:|------|------|
| 全文搜索 | 🔴 P0 | 搜索标题+内容，支持关键词 | 所有平台 |
| 搜索结果高亮 | 🟡 P2 | 匹配关键词在结果中高亮显示 | MediaWiki |
| 最近更新列表 | 🔴 P0 | 按时间倒序展示最近更新的页面 | MediaWiki, Confluence |
| 热门页面 | 🟡 P2 | 按浏览量排序 | MediaWiki |
| 随机页面 | 🔵 P3 | 随机跳转到一个页面 | MediaWiki |
| 搜索建议/自动补全 | 🟡 P2 | 搜索框下拉候选词 | Wikipedia |

---

## RQ6 — 用户系统

| 需求 | 优先级 | 说明 | 参考 |
|------|:--:|------|------|
| 注册/登录 | 🔴 P0 | 用户名+密码注册 | 所有平台 |
| 角色权限 (RBAC) | 🔴 P0 | 管理员/编辑者/只读用户 | MediaWiki, Confluence |
| 个人主页 | 🟡 P2 | 展示用户创建的页面列表 | MediaWiki |
| 用户头像 | 🟡 P2 | 上传或 Gravatar | Confluence, Notion |
| 操作日志/审计 | 🟡 P2 | 记录所有管理操作 | Confluence |

---

## RQ7 — 通知与订阅

| 需求 | 优先级 | 说明 | 参考 |
|------|:--:|------|------|
| 站内消息 | 🟠 P1 | 系统通知（审核结果、评论回复等） | Confluence |
| 页面订阅/关注 | 🟡 P2 | 关注某页面，有更新时收到通知 | MediaWiki, Confluence |
| 邮件通知 | 🔵 P3 | 重要通知发送邮件 | MediaWiki |
| 未读消息徽标 | 🟡 P2 | 导航栏显示未读消息数 | Confluence |

---

## RQ8 — 管理后台

| 需求 | 优先级 | 说明 | 参考 |
|------|:--:|------|------|
| 用户管理 | 🔴 P0 | 列表/搜索/启用/禁用/删除用户 | MediaWiki |
| 页面管理 | 🔴 P0 | 回收站/恢复/永久删除/批量操作 | MediaWiki |
| 分类/标签管理 | 🟠 P1 | CRUD 操作 | MediaWiki |
| 系统设置 | 🟡 P2 | 站点名称/Logo/描述等配置 | Wiki.js |
| 数据统计面板 | 🟡 P2 | 页面数/用户数/今日访问等 | Wiki.js |
| 导入/导出 | 🔵 P3 | 批量导入/导出页面 | MediaWiki |

---

## RQ9 — 展示与体验

| 需求 | 优先级 | 说明 | 参考 |
|------|:--:|------|------|
| 响应式布局 | 🔴 P0 | 桌面端/平板/手机自适应 | 所有现代平台 |
| 暗色模式 | 🟠 P1 | 亮色/暗色主题切换 | Notion, Wiki.js |
| 打印样式 | 🔵 P3 | 打印友好的 CSS | MediaWiki |
| 代码高亮 | 🟡 P2 | 代码块语法高亮 | Wiki.js, Notion |
| 页面加载动画 | 🔵 P3 | 页面切换过渡效果 | — |
| 无障碍 (a11y) | 🟡 P2 | ARIA 标签、键盘导航、色彩对比度 | WCAG 2.1 |

---

## RQ10 — 技术与扩展

| 需求 | 优先级 | 说明 | 参考 |
|------|:--:|------|------|
| REST API | 🔴 P0 | 所有功能通过 API 暴露 | Wiki.js, Confluence |
| API 文档 | 🟠 P1 | Swagger/OpenAPI 自动生成 | — |
| 插件/扩展系统 | 🔵 P3 | 支持第三方扩展 | MediaWiki, Wiki.js |
| Webhook | 🔵 P3 | 页面变更时触发外部回调 | Confluence |
| SSO/OAuth 登录 | 🔵 P3 | 支持 Google/GitHub 等第三方登录 | Wiki.js |
| 多语言支持 (i18n) | 🔵 P3 | 界面多语言切换 | MediaWiki, Wiki.js |

---

## RQ11 — 展示与动效 🆕

> 基于 [Hermes Agent 官方站](https://hermes-agent.nousresearch.com/) 提炼。所有交互必须有动画反馈。

### RQ11.1 品牌视觉升级

| 需求 | 优先级 | 说明 | 参考 |
|------|:--:|------|------|
| 电光蓝暗色背景 | 🔴 P0 | 暗色模式背景从 `#000` 升级为 `#0000f2` | hermes-agent.nousresearch.com |
| 流体缩放系统 | 🟠 P1 | 容器查询驱动 `--u` 单位，全站比例缩放 | hermes-agent.nousresearch.com |
| Hero 大字排版 | 🟠 P1 | Light 字重 (300) + 大写 + 竖排分行 | hermes-agent.nousresearch.com |
| 卡片噪声纹理 | 🟡 P2 | 卡片 hover 时 SVG 噪点叠加层 (mix-blend-mode: screen) | hermes-agent.nousresearch.com |

### RQ11.2 动效规范

| 需求 | 优先级 | 时长 | 缓动 | 用途 |
|------|:--:|:--:|------|------|
| Hover 过渡 | 🔴 P0 | 150ms | ease-out | 链接/按钮/卡片颜色变化 |
| 手风琴展开 | 🔴 P0 | 200ms | ease-out | 分类树子节点 |
| 页面切换 | 🟠 P1 | 200ms/150ms | ease-out/ease-in | 路由 `<Transition>` |
| 主题切换 | 🟠 P1 | 300ms | ease-in-out | 暗色 ↔ 亮色 |
| 骨架屏 | 🟡 P2 | 1.5s | linear infinite | 加载占位 shimmer |
| 卡片抬起 | 🟡 P2 | 200ms | ease-out | hover `translateY(-2px)` |
| 模态框 | 🟡 P2 | 200ms | ease-out | 弹窗 scale+opacity |

### RQ11.3 动效原则

- 禁止瞬时变化（无 transition 的属性突变）
- 使用非对称缓动：进入 `ease-out`，离开 `ease-in`
- 明确列出过渡属性（`color, background, transform, opacity`），禁止 `transition: all`
- 移动端降级：`@media (prefers-reduced-motion) { animation-duration: 0.01ms !important }`

---

## 优先级说明

| 级别 | 含义 | 说明 |
|:--:|------|------|
| 🔴 P0 | 核心必备 | 没有它 Wiki 不完整，MVP 必须实现 |
| 🟠 P1 | 重要功能 | 显著提升用户体验，应尽快实现 |
| 🟡 P2 | 增强功能 | 锦上添花，按资源排期 |
| 🔵 P3 | 远期规划 | 有更好，但不是当前重点 |
