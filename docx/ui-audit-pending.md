---
title: UI 待审核清单
description: 当前 UI 缺陷与待修复项，按严重度排序
domain: audit
status: pending
created: 2026-07-05
---

# UI 待审核清单

> 每条修复后标记 ✅，新发现问题追加到末尾。

## 🔴 P0 — 功能错误

| # | 位置 | 问题 | 修复方案 |
|---|------|------|---------|
| 1 | `Category.vue` L3 | 点击 L3（如"剑"）跳转到新页面，用户要求同页筛选 | `goL3` 不 `router.push`，改设 `l3FilterId` 过滤页面列表 |
| 2 | `Category.vue` L3 | L3 有"全部"选项，v3 spec 要求 L3 不显示"全部" | `l3Tabs` 移除 `{name:'全部'}` 项 |

## 🟠 P1 — 可见性/主题

| # | 位置 | 问题 | 修复方案 |
|---|------|------|---------|
| 3 | 所有 `el-table` | 暗色模式下表头/行/边框仍为白色，不随主题变化 | `hermes-theme.css` 全局覆盖 el-table 暗色样式 |
| 4 | `admin/Categories.vue` | 分类树节点行高过低(4px)，描述文字色太暗不可读 | `min-height:44px` + `cat-tree-name` 用 `var(--fg)` |
| 5 | `admin/Categories.vue` | 删除按钮 hover 前不可见（已修 opacity:0→visible，但边框样式仍弱） | `btn-danger` 用 `border:1px solid` 替代 `border-color` |

## 🟡 P2 — 可改进

| # | 位置 | 问题 | 修复方案 |
|---|------|------|---------|
| 6 | `PageDetail.vue` | TOC 在移动端隐藏，但无替代导航 | 可选：移动端顶部加折叠 TOC |
| 7 | `Search.vue` | 搜索结果摘要截断无渐隐效果 | 可选：加 `line-clamp` + 渐隐遮罩 |
| 8 | 全局 | `[data-theme='light']` 下部分组件未测试 | 需人工验证 |
