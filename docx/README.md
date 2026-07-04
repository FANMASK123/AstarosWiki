---
title: AstarosWiki 技术文档
description: 结构化参考文档索引，按领域分目录，渐进式披露细节
domain: meta
status: final
created: 2026-07-04
updated: 2026-07-04
version: 1.0.0
---

# AstarosWiki 技术文档

> 结构化参考文档，按领域分目录。每个文件聚焦一个主题，渐进式披露细节。

---

## 目录结构

```
docx/
├── README.md                         ← 你在这里
│
├── architecture/                     # 系统架构
│   ├── auth-flow.md                  #   认证流程（JWT、过滤器链、权限矩阵）
│   ├── page-lifecycle.md             #   页面生命周期（状态机、审核流、核心方法）
│   ├── entity-relationships.md       #   实体关系（ER 图、8 张表 DDL、JPA 映射）
│   └── data-flow.md                  #   数据流（请求链路、状态管理、组件通信）
│
├── requirements/                     # 需求规格
│   ├── wiki-requirements.md          #   Wiki 设计需求（参考公开平台）
│   └── gap-analysis.md               #   功能缺口分析（已实现 vs 未实现）
│
├── api/                              # API 参考
│   └── api-routes.md                 #   完整路由表（30+ 端点、请求/响应格式）
│
├── design/                           # 设计规范
│   └── design-system.md              #   Hermes 主题（CSS 变量、组件类、Element Plus 覆盖）
│
└── frontend/                         # 前端规范
    └── frontend-patterns.md          #   开发模式（Store、Router、Component、API 层）
```

---

## 快速导航

| 我想... | 看这个 |
|---------|--------|
| 了解 Wiki 应该有哪些功能 | [`requirements/wiki-requirements.md`](requirements/wiki-requirements.md) |
| 看本项目还缺什么 | [`requirements/gap-analysis.md`](requirements/gap-analysis.md) |
| 理解 JWT 认证流程 | [`architecture/auth-flow.md`](architecture/auth-flow.md) |
| 理解页面状态怎么流转 | [`architecture/page-lifecycle.md`](architecture/page-lifecycle.md) |
| 查看数据库有哪些表 | [`architecture/entity-relationships.md`](architecture/entity-relationships.md) |
| 理解一次请求的完整链路 | [`architecture/data-flow.md`](architecture/data-flow.md) |
| 查某个 API 端点 | [`api/api-routes.md`](api/api-routes.md) |
| 查 CSS 变量/组件类名 | [`design/design-system.md`](design/design-system.md) |
| 学习 Pinia Store 怎么写 | [`frontend/frontend-patterns.md`](frontend/frontend-patterns.md) |

---

## 维护规则

- 每个文件聚焦一个主题，不超过 300 行
- 新增 API → 更新 `api/api-routes.md`
- 新增实体/字段 → 更新 `architecture/entity-relationships.md`
- 状态机变更 → 更新 `architecture/page-lifecycle.md`
- CSS 变量/类变更 → 更新 `design/design-system.md`
- 根目录 `AGENTS.md` 保持精简（<200 行），细节一律放这里
