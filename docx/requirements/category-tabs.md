---
title: 分类导航 — 需求规格
description: 从零分析。不受已有代码影响。
domain: requirements
category: feature-spec
status: final
version: 1.0.0
---

# 分类导航 — 需求规格

## 1. 分类页面 Tab 栏

### 1.1 布局

```
┌────────────────────────────────────────────┐
│  分类标题 (h1)                              │
│                                            │
│  [全部] [武器] [药品] [杂物] [防具] [材料]  │  ← L1：同级分类
│                                            │
│  [全部] [剑] [枪] [戟] [刀]               │  ← L2：选"武器"后出现
│                                            │
│  ─────────────────────────────────────── │
│  页面列表...                                │
└────────────────────────────────────────────┘
```

**规则**：
- L1 栏始终显示。内容 = 当前分类 + 所有兄弟分类。首位是当前分类作为"全部"按钮。
- L2 栏在点击 L1 中某一项后出现。内容 = 被选分类的子分类。首位是"全部"。
- L3 栏同理。
- 三层标签栏占用同一个水平空间，不缩进。

### 1.2 按钮规格

| 属性 | 值 |
|------|----|
| 最小宽度 | 120px |
| 文字对齐 | 居中 |
| 字号 | L1:12px, L2:11px, L3:10px |
| 字体 | Consolas, uppercase, 0.06em |
| 选中态(light) | bg: `color-mix(in srgb, var(--fg) 8%, transparent)` |
| 选中态(dark) | bg: `color-mix(in srgb, var(--fg) 14%, transparent)` |
| 排列 | flex-wrap，超出换行 |

### 1.3 交互

| 操作 | 结果 |
|------|------|
| 点击 L1 某项 | 该项高亮，L2 栏滑入显示该项的子分类。URL 更新但不刷新页面 |
| 点击 L2 某项 | 该项高亮，L3 栏滑入显示该项的子分类 |
| 点击 L3 某项 | 页面列表刷新为该分类下的页面 |

### 1.4 数据来源

- 分类详情：`getCategoryById(id)` — 不含子节点
- 子分类列表：从 Pinia store `categoryStore.categories` 树递归查找
- 页面列表：`getPagesByCategory(filterId, page, size)`

### 1.5 不重复

同一分类名称只能出现在一个层级中。L1 中已存在的项不出现在 L2/L3。

---

## 2. 管理员三级创建约束

| 层级 | 能否创建子分类 |
|------|:--:|
| 一级 | ✅ |
| 二级 | ✅ |
| 三级 | ❌ "已达最大深度" |
| 四级及以上 | ❌ 不存在，无法创建 |

### 前端

- el-tree-select 中 depth ≥ 3 的选项不可选 / 不显示
- 树节点 level ≥ 3 时 "+" 按钮置灰

### 后端

```
if (parentLevel >= 3) throw BusinessException("分类最多支持三级")
```

---

## 3. 删除分类 + 页面迁移

管理员删除一个分类：

```
是否有子分类？→ 阻断"请先删除子分类"
    ↓否
是否有页面引用？→ 弹窗选迁移目标 → 点"迁移并删除" → 完成
    ↓否
直接确认删除
```

### 后端 API

```
DELETE /api/categories/{id}?migrateTo={targetId}
```

有 `migrateTo`：先 `UPDATE pages SET category_id=? WHERE category_id=?` 再 `DELETE`
无 `migrateTo`：直接 `DELETE`

---

## 4. 管理员 UI 一致性

所有 /admin/* 页面：`eyebrow` + `section-title` 标题，`el-table` 暗色适配，`btn-primary/btn-ghost` 按钮，`el-dialog` 统一弹窗，`el-pagination` 统一分页。

---

## 5. 侧边栏

功能菜单 → 分类导航(可展开) → 右滑面板 L1→L2→L3。
