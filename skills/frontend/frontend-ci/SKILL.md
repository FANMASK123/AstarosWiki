---
name: frontend-ci
description: "Use when frontend lint/test/build fails or CI shows red. Covers the no-console vs R3 conflict pattern and the --max-warnings 0 trap."
version: 0.1.0
author: AstarosWiki Team
license: MIT
metadata:
  hermes:
    tags: [frontend, ci, eslint, vitest, vite, harness]
    category: frontend
    related_skills: [project-harness, vue-category-navigation]
---

# Frontend CI Pipeline

## When to Use

- `pnpm lint` / `pnpm test` / `pnpm build` 失败
- GitHub Actions frontend job 红
- `eslint .` 报 `no-undef` / `no-console` / `no-explicit-any`
- `--max-warnings 0` 把 warn 升级为失败

## Quick Reference

```bash
cd astaros-wiki-frontend
pnpm lint    # 0 errors required (warnings OK)
pnpm test    # vitest run
pnpm build   # vue-tsc -b && vite build
```

CI 流程：`.github/workflows/ci.yml` → `pnpm install --frozen-lockfile → lint → test → build`

## 关键经验

### 经验 1：浏览器 API 未声明 → `no-undef`

- **症状**：`'MutationObserver' is not defined  no-undef`（error 级，直接阻断）
- **根因**：ESLint flat config 不自动识别浏览器全局变量
- **修复**：
  ```js
  // eslint.config.js → languageOptions.globals
  MutationObserver: 'readonly',
  ```
- **预防**：新增浏览器 API（ResizeObserver、IntersectionObserver 等）时同步加 globals

### 经验 2：`no-console` vs AGENTS.md R3 冲突

- **冲突三角**：
  - R3 规则：所有 catch 块必须 `console.error`
  - ESLint 默认：`no-console: warn`
  - CI 用 `lint:strict`（`--max-warnings 0`）→ warn = fail
- **解决（路线 A）**：
  1. ESLint 允许 error：`'no-console': ['warn', { allow: ['error'] }]`
  2. CI 降级：`pnpm lint:strict` → `pnpm lint`
  3. 所有 catch 块统一：`catch (error) { console.error(error); /* ... */ }`
- **反模式**：不要在 catch 里写 `console.log`/`console.debug`，它们仍触发 warning

### 经验 3：catch 块必须使用 error 变量

- **症状**：`'error' is defined but never used  @typescript-eslint/no-unused-vars`
- **根因**：只写了 `catch (error)` 但未调用
- **标准模式**：
  ```ts
  try {
    // ...
  } catch (error) {
    console.error(error)  // 消除 unused-vars + R3 合规
    ElMessage.error('操作失败')
    return false
  }
  ```

## Technical Debt Baseline (2025-07-07)

当前 `pnpm lint` 输出：**0 errors, 23 warnings**

| 文件 | warning 数 | 类型 |
|------|:----------:|------|
| `src/api/request.ts` | 11 | no-explicit-any |
| `src/api/page.ts` | 2 | no-explicit-any |
| `src/views/Register.vue` | 6 | no-explicit-any |
| `src/views/admin/Categories.vue` | 3 | no-explicit-any |
| `src/views/page/PagePublish.vue` | 1 | no-explicit-any |
| **合计** | **23** | no-explicit-any |

> 新增 warning 必须说明理由。如数量增长需更新本基线。

## Common Pitfalls

| 陷阱 | 后果 | 规避 |
|------|------|------|
| 忘记 `cd astaros-wiki-frontend` | pnpm 找不到 package.json | CI 用 `defaults.run.working-directory` |
| 把 catch 的 `error` 删掉"消除 warning" | 违反 R3 + 丢调试信息 | 统一 `console.error(error)` |
| 在 catch 里用 `console.log` | 触发 no-console warning | 只用 `console.error` |
| 新增浏览器 API 忘加 globals | no-undef error 阻断 CI | 加完跑一次 `pnpm lint` |
| `lint:strict` 本地跑会因 23 warnings 失败 | 混淆判断 | 本地用 `pnpm lint`，strict 仅作参考 |

## Verification Checklist

- [ ] `pnpm lint` exit 0（warnings 不阻断）
- [ ] `pnpm test` exit 0
- [ ] `pnpm build` exit 0
- [ ] 所有 catch 块有 `console.error(error)`
- [ ] 新增的浏览器 API 在 `eslint.config.js` globals 中声明
- [ ] warning 数量 ≤ 23（当前基线）

## Related

- `docx/frontend/frontend-patterns.md` — 反模式清单
- `AGENTS.md` R1-R5 — 刚性规则
- `.github/workflows/ci.yml` — CI 流程定义
- `eslint.config.js` — ESLint 规则配置
