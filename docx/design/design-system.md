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

---

# 官方站实现深度解析

> 基于 [hermes-agent.nousresearch.com](https://hermes-agent.nousresearch.com/) 首页完整 DOM/CSS 逆向分析。
> 以下每个模式均标注了源码证据和在我们的 Vue 3 + Element Plus 环境中的适配方案。

---

## 1. 流体缩放系统

### 官方实现

```css
.hermes-web {
  --u: calc(100cqw / 2360);           /* 基准：2360px 设计稿 → 1u = 1px */
  --hw-gutter: calc(210 * var(--u));  /* 页边距 = 210px @2360px 宽 */
  --hw-gap: calc(30 * var(--u));      /* 间距 = 30px */
}

@media (max-width: 767px) {
  .hermes-web {
    --u: calc(100cqw / 760);          /* 移动端基准 760px */
    --hw-gutter: calc(48 * var(--u));
  }
}
```

**原理**：`container-type: inline-size` + `cqw` 使页面所有尺寸（margin, padding, font-size, gap）随视口宽度线性缩放。2360px 宽时 1u=1px，1180px 时 1u=0.5px。

### 适配到 AstarosWiki

```css
/* 我们的主题已实现简化版 */
:root { --u: calc(100cqw / 2360); }
```

**待深化**：将 `padding: 24px` 替换为 `padding: calc(24 * var(--u))`；将 `gap: 16px` 替换为 `gap: calc(16 * var(--u))`。

---

## 2. 全局半透明 + 混合模式

### 官方实现

```css
/* 按钮 hover — 亮色文字按钮在暗色背景上变白 */
.bg-hermes-fg { background: #f5f5f5; }
.hover\:bg-white:hover { background: #fff; }

/* 英雄图 — 混合到蓝色背景中 */
.mix-blend-lighten { mix-blend-mode: lighten; }

/* 卡片噪声 — 屏幕混合微弱的噪点 */
.mix-blend-screen { mix-blend-mode: screen; opacity: 0.04; }

/* 半透明边框 — 不使用 rgba，使用 color-mix 或直接透明色 */
--color-border: #f8fafc21;  /* 13% 不透明度 */
```

### 适配到 AstarosWiki

```css
/* 我们的主题已实现 */
[data-theme='dark'] {
  --color-card: rgba(255,255,255,0.06);  /* 半透明卡片 */
  --color-border: rgba(245,245,245,0.15); /* 半透明边框 */
}
.hw-card::after { mix-blend-mode: screen; opacity: 0.06; } /* hover 噪声 */
```

---

## 3. 噪声纹理覆盖层

### 官方实现

官方站使用两种噪声纹理：

**全局噪声**（固定覆盖层）：
```css
.noise-overlay {
  position: fixed; inset: 0; z-index: 101;
  pointer-events: none;
  mix-blend-mode: multiply;
  opacity: 0.035;
  background-image: url("data:image/svg+xml,..."); /* SVG turbulence filter */
  background-size: 512px 512px;
}
[data-theme='dark'] .noise-overlay {
  mix-blend-mode: screen;
  opacity: 0.06;
}
```

**卡片噪声**（`group-hover` 触发）：
```css
.download-card {
  background-image: url(...); /* 平台艺术图 */
}
.download-card-wash {
  /* 半透明遮罩层，让图片变暗 */
  background: color-mix(in srgb, var(--color-background) 46%, transparent);
}
.download-arc {
  /* 装饰弧线 */
  border: 1px solid color-mix(in srgb, var(--color-foreground) 35%, transparent);
  border-bottom-color: transparent;
  border-radius: 999px 999px 0 0;
}
```

### 适配到 AstarosWiki

已在 `hermes-theme.css` 实现全局噪声和卡片 hover 噪声。待实现：卡片装饰弧线。

```css
/* 待添加到 .hw-card */
.hw-card-arc {
  position: absolute; inset: 11%;
  border: 1px solid var(--color-muted);
  border-bottom-color: transparent;
  border-radius: 999px 999px 0 0;
  opacity: 0.25;
  transition: opacity 300ms;
}
.hw-card:hover .hw-card-arc { opacity: 0.45; }
```

---

## 4. 排版模式

### 官方实现

| 用途 | 字体 | 字重 | 大小 | 行高 | 字间距 |
|------|------|:--:|------|:--:|:--:|
| Hero 标题 | Sigurd | 300 | `calc(132 * var(--u))` | 0.88 | 0.03em |
| 区域标题 | Sigurd | 300 | `calc(64 * var(--u))` | 1 | — |
| 标签文字 | Courier Prime | 400 | `var(--hw-text-eyebrow)` | — | 0.18em |
| 正文 | Courier Prime | 400 | `var(--hw-text-body)` | 1.4 | — |
| 导航链接 | Sigurd | 800 | `clamp(0.8rem, calc(22 * var(--u)), 1.35rem)` | — | 0.03em |

**关键细节**：
- Hero 标题不是一行，而是 **竖排多个 `<span>`**，每个单词一行
- 所有文字 **全大写** (`text-transform: uppercase`)
- 标题使用 **light 字重**（300），不是 bold
- 正文使用 **Courier Prime**（等宽字体），不是无衬线

### 适配到 AstarosWiki

```html
<!-- Hero 标题 — 已在 Home.vue 实现 -->
<h1 class="hw-hero-title">
  <span>阿斯塔洛斯</span>
  <span>Wiki</span>
</h1>

<!-- 正文 — 保持 Segoe UI，Wiki 内容不适合 Courier -->
<p class="hw-mono">元信息使用等宽</p>
```

---

## 5. 平台下载卡片

### 官方实现

```html
<article class="group relative flex min-h-[21rem] flex-col items-center justify-center overflow-hidden border p-8 text-center">
  <!-- 背景图片 + 缩放动效 -->
  <img class="absolute inset-0 h-full w-full object-cover opacity-45 transition duration-500 group-hover:scale-105 group-hover:opacity-60" />
  <!-- 半透明遮罩 -->
  <div class="absolute inset-0 bg-[color-mix(in_srgb,var(--color-background)_46%,transparent)]"></div>
  <!-- 装饰弧线 -->
  <span class="absolute inset-[11%] border border-[color-mix(in_srgb,var(--color-foreground)_35%,transparent)] border-b-transparent rounded-[999px_999px_0_0] opacity-55 translate-y-[18%]"></span>
  <!-- 内容 -->
  <div class="relative z-10 flex flex-col items-center gap-6">
    <p>macOS 12+</p>
    <h3 class="text-6xl">Mac OS</h3>
    <a class="bg-foreground text-background px-6 py-4">前往下载</a>
  </div>
</article>
```

### 适配到 AstarosWiki

用于首页"快捷入口"卡片，替换纯色卡片为图片背景卡片：

```html
<router-link to="/page/夜族" class="hw-card hw-card-platform">
  <div class="hw-card-wash"></div>
  <span class="hw-card-arc"></span>
  <div class="hw-card-platform-content">
    <h3 class="hw-card-title">夜族</h3>
    <p class="hw-mono">探索夜族的神秘世界</p>
  </div>
</router-link>
```

```css
.hw-card-platform {
  min-height: 240px;
  background-image: var(--hw-platform-img, none);
  background-size: cover;
  background-position: center;
}
.hw-card-wash {
  position: absolute; inset: 0;
  background: color-mix(in srgb, var(--color-background) 46%, transparent);
}
.hw-card-platform-content {
  position: relative; z-index: 1;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  height: 100%;
}
```

---

## 6. 终端安装命令块

### 官方实现

```html
<div class="border rounded-[4px] bg-white">
  <!-- Tab 栏 -->
  <div class="flex border-b px-4 pt-3" role="tablist">
    <button class="border-b-2 border-hermes text-hermes font-bold">macOS / Linux</button>
    <button class="border-transparent text-hermes/45">Windows</button>
  </div>
  <!-- 命令内容 -->
  <div class="px-4 py-3">
    <code>
      <span class="text-hermes/45">curl </span>
      <span class="text-hermes font-bold">-fsSL https://...install.sh</span>
      <span class="text-hermes/45"> | bash</span>
    </code>
    <button aria-label="Copy">📋</button>
  </div>
</div>
```

### 适配到 AstarosWiki

用于 API 文档展示或安装指南：

```html
<div class="hw-terminal">
  <div class="hw-terminal-tabs">
    <button class="hw-terminal-tab active">cURL</button>
    <button class="hw-terminal-tab">Python</button>
  </div>
  <div class="hw-terminal-body">
    <code>curl -X POST http://localhost:8801/api/auth/login ...</code>
    <button class="hw-terminal-copy" @click="copy">📋</button>
  </div>
</div>
```

```css
.hw-terminal {
  border: 1px solid var(--color-border);
  background: var(--color-card);
  overflow: hidden;
}
.hw-terminal-tabs {
  display: flex; gap: 0; border-bottom: 1px solid var(--color-border);
}
.hw-terminal-tab {
  padding: 10px 18px; font-family: var(--font-mono); font-size: 0.75rem;
  border: none; border-bottom: 2px solid transparent; background: none;
  color: var(--color-muted); cursor: pointer; transition: all 150ms;
}
.hw-terminal-tab.active {
  color: var(--color-foreground); border-bottom-color: var(--color-foreground);
  font-weight: 700;
}
.hw-terminal-body {
  display: flex; align-items: center; padding: 14px 18px;
  font-family: var(--font-mono); font-size: 0.85rem;
}
.hw-terminal-body code { flex: 1; }
.hw-terminal-copy {
  background: none; border: none; color: var(--color-muted);
  cursor: pointer; padding: 4px; transition: color 150ms;
}
.hw-terminal-copy:hover { color: var(--color-foreground); }
```

---

## 7. 视频预览框架

### 官方实现

```html
<div class="relative aspect-[2174/1273] w-full overflow-hidden">
  <video autoplay loop muted playsinline poster="showcase.webp" class="absolute inset-0 object-cover"></video>
  <!-- 粗边框遮罩 -->
  <div class="pointer-events-none absolute inset-0 border-[calc(55*var(--u))] mix-blend-lighten max-md:border-[16px]"></div>
</div>
```

**关键技巧**：使用 `border-[calc(55*var(--u))]` 做视频的边框遮罩，混合模式 `lighten` 使边框色与背景融合。

### 适配到 AstarosWiki

用于首页展示截图或功能预览：

```html
<figure class="hw-preview">
  <div class="hw-preview-frame">
    <img :src="screenshot" class="hw-preview-img" />
    <div class="hw-preview-border"></div>
  </div>
  <figcaption class="hw-mono">AstarosWiki 预览</figcaption>
</figure>
```

```css
.hw-preview-frame {
  position: relative; aspect-ratio: 16/10; overflow: hidden;
  border: 1px solid var(--color-border);
}
.hw-preview-img {
  width: 100%; height: 100%; object-fit: cover;
}
.hw-preview-border {
  position: absolute; inset: 0;
  border: clamp(8px, 4vw, 55px) solid var(--color-background);
  mix-blend-mode: lighten; pointer-events: none;
}
```

---

## 8. 数字编号板块

### 官方实现

六大功能板块使用 `#1` ~ `#6` 编号，每个板块包含：
- 编号标签（mono, uppercase, tracked）
- 大标题（light weight, uppercase）
- 图片（mix-blend-screen）
- 描述文字（mono, body size）

```html
<article>
  <p class="hw-mono tracking-[0.18em] opacity-80">#1 Connect</p>
  <h2 class="text-[calc(64*var(--u))] leading-none font-light">Lives Everywhere</h2>
  <div class="aspect-[666/574] bg-hermes overflow-hidden">
    <img class="mix-blend-screen object-cover" />
  </div>
  <p class="hw-mono leading-[1.4] opacity-90">Telegram, Discord, Slack...</p>
</article>
```

### 适配到 AstarosWiki

用于首页功能展示或管理后台 Dashboard：

```html
<article class="hw-feature">
  <div class="hw-feature-header">
    <p class="hw-eyebrow">#1 创作</p>
    <h2 class="hw-section-title">页面管理</h2>
  </div>
  <div class="hw-feature-visual">
    <img src="..." class="hw-feature-img" />
  </div>
  <p class="hw-mono">创建、编辑、审核 Wiki 页面，支持 Markdown 和版本历史。</p>
</article>
```

---

## 9. Footer 巨幅文字

### 官方实现

```html
<div class="text-center text-[clamp(6rem,22vw,22rem)] uppercase leading-none opacity-90">Hermes</div>
```

巨幅品牌文字作为视觉收尾，使用超大 `clamp` 值（最大 22rem），`leading-none` 紧凑排列。

### 适配到 AstarosWiki

```html
<div class="hw-footer-wordmark">阿斯塔洛斯</div>
```

```css
.hw-footer-wordmark {
  font-size: clamp(3rem, 15vw, 15rem);
  font-weight: 300; text-transform: uppercase;
  line-height: 0.85; letter-spacing: 0.02em;
  text-align: center; opacity: 0.15;
  margin-top: 4rem; pointer-events: none; user-select: none;
}
```

---

## 10. 边框分隔模式

### 官方实现

导航栏每个链接之间用 `border-r border-border` 分隔，而非传统间距：

```html
<nav class="flex items-stretch">
  <a class="border-r border-border px-4">功能</a>
  <a class="border-r border-border px-4">桌面版</a>
  <a class="border-r border-border px-4">文档</a>
  <a class="px-4">GitHub</a>  <!-- 最后一个无边线 -->
</nav>
```

### 适配到 AstarosWiki

已在 `hw-nav-link` 实现：`border-right: 1px solid var(--color-border)`。

---

## 11. `color-mix()` 透明度技巧

### 官方实现

官方站大量使用 `color-mix(in srgb, ...)` 而非 `rgba()` 或 `opacity`：

```css
/* 半透明前景色 — 替代 opacity: 0.45 */
color: color-mix(in srgb, var(--color-foreground) 45%, transparent);

/* 半透明背景 — 替代 rgba(255,255,255,0.06) */
background: color-mix(in srgb, var(--color-background) 46%, transparent);

/* 半透明边框 — 替代 rgba(245,245,245,0.15) */
border-color: color-mix(in srgb, var(--color-foreground) 15%, transparent);
```

**优势**：`color-mix()` 可以精确控制混合比例，且支持 CSS 变量动态计算，而 `rgba()` 只能硬编码颜色。

### 适配到 AstarosWiki

```css
/* 替换现有的 rgba 写法 */
[data-theme='dark'] {
  --color-card: color-mix(in srgb, var(--color-foreground) 6%, transparent);
  --color-border: color-mix(in srgb, var(--color-foreground) 15%, transparent);
  --color-muted: color-mix(in srgb, var(--color-foreground) 55%, transparent);
}
```

---

## 12. 导航结构

### 官方实现

导航不使用 `justify-between` 的传统布局，而是 **绝对定位 + 分散摆放**：

```
[Nous] ────────── [Hermes Agent] ────────── [Portal] [Install→]
 左链接              居中 Logo               右链接 + CTA
```

```html
<nav class="absolute inset-x-0 top-0 z-20 flex items-start justify-between px-gutter pt-12">
  <a>Nous</a>
  <a>Docs</a>
  <div class="flex flex-col items-center">
    <a>Hermes<br/>Agent</a>
    <span> <!-- Discord + GitHub icons --> </span>
  </div>
  <a>Portal</a>
  <a class="inline-flex items-center gap-2">Install →</a>
</nav>
```

**关键**：导航链接不放在同一行，而是 `justify-between` 分散到两侧和中间。

---

## 技术债务 / 待实现清单

| # | 模式 | 状态 | 优先级 |
|---|------|:--:|:--:|
| 1 | 流体缩放 `--u` 全面铺开 | ⚠️ 仅 CSS 变量定义，未在间距中使用 | P1 |
| 2 | `color-mix()` 替换 `rgba()` | ❌ 未实现 | P1 |
| 3 | 平台下载卡片样式 | ❌ 未实现 | P2 |
| 4 | 终端命令块组件 | ❌ 未实现 | P2 |
| 5 | 视频/图片预览边框遮罩 | ❌ 未实现 | P3 |
| 6 | 数字编号板块 | ❌ 未实现 | P3 |
| 7 | Footer 巨幅文字 | ❌ 未实现 | P3 |
| 8 | 卡片装饰弧线 | ❌ 未实现 | P3 |
