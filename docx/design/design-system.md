---
title: 设计系统 v2.0
description: 融合 Hermes Agent 官方设计 DNA（#0000f2 电光蓝、流体缩放、大写排版）与当前 Wiki 功能布局的新版设计规范
domain: design
category: ui-framework
version: 2.0.0
status: draft
created: 2026-07-04
updated: 2026-07-04
references:
  - https://hermes-agent.nousresearch.com/
  - https://hermes-ai.net/zh/desktop/
related_sources:
  - a-staros-wiki-frontend/src/assets/hermes-theme.css
  - a-staros-wiki-frontend/src/App.vue
---

# 设计系统 v2.0

> 基于 [Hermes Agent 官方站](https://hermes-agent.nousresearch.com/) 设计 DNA，融合当前 Wiki 功能布局。

---

## 🔵 核心视觉 DNA（来自官方站）

### 品牌色

```
--hw-brand: #0000f2;       /* 电光蓝 — Hermes Agent 官方背景色 */
--hw-brand-fg: #f5f5f5;    /* 蓝底上的前景色 — 暖白而非纯白 */
```

**设计决策**：暗色模式下，页面背景从 `#000` 改为 `#0000f2`（电光蓝）。亮色模式保持白色背景。这是与官方站最核心的对齐点。

### 排版系统

| 层级 | 字体 | 字重 | 大小 | 样式 |
|------|------|:--:|------|------|
| Hero 标题 | Segoe UI / Sigurd | 300 (light) | `clamp(3rem, 8vw, 8rem)` | 大写，`line-height: 0.88`，`letter-spacing: 0.03em` |
| 区域标题 | Segoe UI / Sigurd | 300 | `clamp(2rem, 5vw, 4rem)` | 大写 |
| 卡片标题 | Segoe UI | 700 | `1.25rem` | 大写 `0.04em` |
| 正文 | Segoe UI | 400 | `1rem` | 正常大小写 |
| 标签/元信息 | Consolas | 400 | `0.75rem` | 大写 `0.14em`，暗度 `0.8` |
| 计数/代码 | Consolas | 700 | `0.875rem` | 正常大小写 |

### 流体缩放

```css
/* 容器查询驱动的缩放单元 — 页面所有间距/字号由此派生 */
:root {
  --u: calc(100cqw / 2360);        /* 基准：2360px 设计稿 */
  --hw-gutter: calc(210 * var(--u)); /* 页边距 */
  --hw-gap: calc(30 * var(--u));     /* 网格间距 */
}

@media (max-width: 768px) {
  :root {
    --u: calc(100cqw / 760);        /* 移动端基准 */
    --hw-gutter: calc(48 * var(--u));
  }
}
```

---

## 🎨 色彩体系 v2.0

### 亮色模式

| Token | 值 | 用途 |
|-------|----|------|
| `--color-background` | `#fff` | 页面背景 |
| `--color-foreground` | `#111827` | 正文 |
| `--color-card` | `#f8fafc` | 卡片 |
| `--color-border` | `#11182724` | 边框 |
| `--color-muted` | `#64748b` | 次要文字 |
| `--color-accent` | `#0000f2` | 链接/强调 |
| `--color-accent-fg` | `#f5f5f5` | 强调色上的文字 |

### 暗色模式（Hermes 官方蓝）

| Token | 值 | 用途 |
|-------|----|------|
| `--color-background` | `#0000f2` | 🔵 电光蓝背景 |
| `--color-foreground` | `#f5f5f5` | 暖白正文 |
| `--color-card` | `rgba(255,255,255,0.06)` | 半透明卡片 |
| `--color-border` | `rgba(245,245,245,0.15)` | 半透明边框 |
| `--color-muted` | `rgba(245,245,245,0.55)` | 次要文字 |
| `--color-accent` | `#f5f5f5` | 强调/按钮 |
| `--color-accent-fg` | `#0000f2` | 强调色上的文字 |

### 主题切换

```html
<html data-theme="dark">  或  <html data-theme="light">
```
由 `App.vue` 的 `toggleTheme()` 管理，持久化到 `localStorage['hermes-theme']`。

---

## 🧩 布局系统 v2.0

### 全局结构

```
┌─────────────────────────────────────────────────────┐
│  Header (fixed, blur backdrop)                      │
│  ┌──────────┬──────────────────────────────────────┐│
│  │  Logo    │  Nav links (border-right separated)  ││
│  │          │                    Search · Theme · …││
│  └──────────┴──────────────────────────────────────┘│
├────────┬────────────────────────────────────────────┤
│        │                                            │
│ Sidebar│  <router-view>                             │
│  200px │    max-width: 1400px                       │
│        │    padding: var(--hw-gutter)               │
│  (可折叠) │                                          │
│        │                                            │
├────────┴────────────────────────────────────────────┤
│  Footer (3-col grid, border-top)                    │
└─────────────────────────────────────────────────────┘
```

**变更**：
- Header 增加 `backdrop-filter: blur(12px)` + 半透明背景
- 主内容区使用 `--hw-gutter` 流体间距替代固定 `24px`
- Sidebar 宽度从 `200px` 改为可随 `--u` 缩放

### 页面模板

#### Hero 页面（首页）

```html
<section class="hero">
  <p class="hw-eyebrow">Open Source · MIT License</p>
  <h1 class="hw-hero-title">
    <span>阿斯塔洛斯</span>
    <span>Wiki</span>
  </h1>
  <p class="hw-hero-desc">探索四大本源力量交织的奇幻世界</p>
  <div class="hw-hero-actions">
    <a class="hw-btn-primary">开始探索 →</a>
    <a class="hw-btn-ghost">创建页面 ↗</a>
  </div>
</section>
```

样式：
```css
.hw-hero-title {
  font-size: clamp(3rem, 8vw, 8rem);
  font-weight: 300;
  line-height: 0.88;
  letter-spacing: 0.03em;
  text-transform: uppercase;
  display: flex;
  flex-direction: column;
}
.hw-hero-title span {
  display: block;
}
```

#### 列表/网格页面（分类、搜索、管理后台）

```html
<section class="hw-section">
  <p class="hw-eyebrow">浏览</p>
  <h2 class="hw-section-title">分类导航</h2>
  <div class="hw-grid hw-grid--3col">
    <article class="hw-card">...</article>
  </div>
</section>
```

#### 详情页面

```html
<article class="hw-article">
  <p class="hw-eyebrow">页面</p>
  <h1 class="hw-article-title">{{ title }}</h1>
  <div class="hw-article-meta">作者 · 版本 · 浏览</div>
  <hr class="hw-divider" />
  <div class="hw-prose">{{ content }}</div>
</article>
```

---

## 🎯 组件 v2.0

### 按钮

```html
<!-- 主按钮：白底蓝字（暗色模式） / 蓝底白字（亮色模式） -->
<button class="hw-btn-primary">
  <span class="hw-btn-label">提交</span>
  <span class="hw-btn-arrow">→</span>
</button>

<!-- 幽灵按钮：透明 + 边框 -->
<button class="hw-btn-ghost">取消</button>
```

| 类 | 背景 | 文字 | 边框 | Hover |
|----|------|------|------|-------|
| `hw-btn-primary` | `var(--color-foreground)` | `var(--color-background)` | 无 | `hover:bg-white`（暗色模式下） |
| `hw-btn-ghost` | 透明 | `var(--color-foreground)` | `1px solid var(--color-border)` | `bg-foreground/10` |

**动效**：`transition: all 200ms ease-out`

### 卡片

```html
<article class="hw-card">
  <div class="hw-card-noise"></div>  <!-- 噪声纹理覆盖层 -->
  <h3 class="hw-card-title">标题</h3>
  <p class="hw-card-desc">描述</p>
</article>
```

| 状态 | 样式 |
|------|------|
| 默认 | `bg-card`, `border border-border` |
| Hover | `border-color → var(--color-muted)`, `transform: translateY(-2px)` |
| Noise | `::after` 伪元素, `mix-blend-mode: screen`, `opacity: 0.04` |

**动效**：`transition: border-color 200ms, transform 200ms ease-out`

### 导航链接

```html
<router-link class="hw-nav-link" active-class="active">标签</router-link>
```

| 状态 | 样式 |
|------|------|
| 默认 | `text-muted`, `border-right: 1px solid var(--color-border)` |
| Hover | `text-foreground`, `bg-foreground/5` |
| Active | `text-foreground`, `bg-foreground/8` |

**动效**：`transition: all 150ms ease-out`

### 排版组件

| 类 | 用途 | 大小 | 字重 | 样式 |
|----|------|------|:--:|------|
| `hw-eyebrow` | 区域标签 | `0.8rem` | 400 | mono, uppercase, `0.18em` |
| `hw-section-title` | 区域标题 | `clamp(1.8rem, 4vw, 3rem)` | 300 | uppercase, `0.03em`, `line-height: 0.95` |
| `hw-article-title` | 文章标题 | `clamp(1.5rem, 3vw, 2.5rem)` | 700 | uppercase, `0.04em` |
| `hw-mono` | 元信息 | `0.75rem` | 400 | mono, uppercase, `0.12em` |
| `hw-divider` | 分割线 | — | — | `1px solid var(--color-border)`, `margin: 2rem 0` |

### 标签 & 徽标

```html
<span class="hw-tag">分类名</span>
<span class="hw-badge">12</span>
```

| 类 | 样式 |
|----|------|
| `hw-tag` | `font-mono`, `0.7rem`, uppercase, `1px border-border`, `padding: 2px 8px` |
| `hw-badge` | `font-mono`, `0.65rem`, `bg-foreground/10`, `padding: 1px 6px` |

### 手风琴树（分类导航）

```html
<ul class="hw-tree">
  <li class="hw-tree-node">
    <button class="hw-tree-toggle" :class="{ open }">▸</button>
    <a class="hw-tree-link">分类名</a>
    <span class="hw-badge">3</span>
  </li>
  <Transition name="hw-expand">
    <ul v-if="open" class="hw-tree-children">...</ul>
  </Transition>
</ul>
```

**动效**：
```css
.hw-tree-toggle { transition: transform 200ms ease-out; }
.hw-tree-toggle.open { transform: rotate(90deg); }

.hw-expand-enter-active { transition: all 200ms ease-out; }
.hw-expand-leave-active { transition: all 150ms ease-in; }
```

### 骨架屏（替换旧「加载中...」文字）

```html
<div class="hw-skeleton">
  <div class="hw-skeleton-line w-75"></div>
  <div class="hw-skeleton-line w-50"></div>
  <div class="hw-skeleton-line w-100"></div>
</div>
```

```css
.hw-skeleton-line {
  height: 12px;
  background: var(--color-border);
  border-radius: 2px;
  animation: hw-shimmer 1.5s infinite;
}
@keyframes hw-shimmer {
  0%   { opacity: 0.3; }
  50%  { opacity: 0.6; }
  100% { opacity: 0.3; }
}
```

### 页面过渡

```html
<router-view v-slot="{ Component }">
  <Transition name="hw-page" mode="out-in">
    <component :is="Component" />
  </Transition>
</router-view>
```

```css
.hw-page-enter-active { transition: all 200ms ease-out; }
.hw-page-leave-active { transition: all 150ms ease-in; }
.hw-page-enter-from { opacity: 0; transform: translateY(8px); }
.hw-page-leave-to   { opacity: 0; transform: translateY(-8px); }
```

---

## 🎬 动效规范

| 类型 | 时长 | 缓动 | 用途 |
|------|:--:|------|------|
| 微交互 | `150ms` | `ease-out` | hover, focus, toggle |
| 组件过渡 | `200ms` | `ease-out` | 卡片 hover, 菜单展开/收起 |
| 页面切换 | `200ms enter / 150ms leave` | `ease-out / ease-in` | 路由切换 |
| 骨骼屏 | `1.5s` | `linear infinite` | 加载占位 |
| 主题切换 | `300ms` | `ease-in-out` | 亮色 ↔ 暗色 |

**原则**：
- 所有交互必须有 `transition` 声明
- 不写 `all` 过渡 — 明确列出属性（`color`, `background`, `transform`, `opacity`, `border-color`）
- 使用 `ease-out`（进入） / `ease-in`（离开）非对称曲线

---

## Element Plus 全局覆盖 v2.0

| 组件 | 覆盖 |
|------|------|
| `el-button--primary` | `bg-foreground text-background`，`border-radius: 0`，`transition: all 200ms ease-out` |
| `el-input__wrapper` | `bg-background`，`border-border`，`border-radius: 0`，`:focus → border-foreground` |
| `el-dialog` | `bg-background`，`border-border`，`border-radius: 0`，`box-shadow: 0 20px 60px rgba(0,0,0,0.3)` |
| `el-dialog__header` | `border-bottom border-border`，`padding: 20px 24px` |
| `el-table th` | `bg-background`，`font-mono uppercase tracked`，`border-bottom border-border` |
| `el-tabs__item` | `uppercase tracked`，`transition: color 150ms` |
| `el-message` | `border-radius: 0`，`border border-border` |
| `el-pagination` | 按钮 `border-radius: 0`，`border-border` |

---

## 迁移清单（v1.0 → v2.0）

| 旧类名 (v1.0) | 新类名 (v2.0) | 说明 |
|---------------|---------------|------|
| `btn-hermes-primary` | `hw-btn-primary` | 主按钮 |
| `btn-hermes-outline` | `hw-btn-ghost` | 幽灵按钮 |
| `hermes-card` | `hw-card` | 卡片 |
| `hermes-card-title` | `hw-card-title` | 卡片标题 |
| `nav-link` | `hw-nav-link` | 导航链接 |
| `section-label` | `hw-eyebrow` | 区域标签 |
| `section-title` | `hw-section-title` | 区域标题 |
| `hermes-tag` | `hw-tag` | 标签 |
| `hermes-divider` | `hw-divider` | 分割线 |
| — | `hw-badge` | 🆕 计数徽标 |
| — | `hw-skeleton` | 🆕 骨架屏 |
| — | `hw-tree` | 🆕 手风琴树 |
| — | `hw-page` (Transition) | 🆕 页面过渡 |
| — | `hw-hero-title` | 🆕 Hero 标题 |
| `--color-*` (黑底) | `--color-*` (蓝底 `#0000f2`) | 暗色模式背景色变更 |

> ⚠️ v1.0 类名在迁移期间保留兼容，所有旧类添加 `@deprecated` 注释。新代码必须使用 `hw-*` 前缀。
