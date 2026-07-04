---
title: Harness 流程与环境审计
description: 全链路差距分析 — 构建检查、测试覆盖、安全扫描、CI/CD、文档同步
domain: requirements
category: audit
status: final
created: 2026-07-04
updated: 2026-07-04
version: 1.0.0
---

# Harness 流程与环境审计

> 对照「闭环测试与反馈」「架构约束与护栏」原则，逐层审计当前工程流程。

---

## 现状总览

| 层 | 已有 | 缺失 |
|----|------|------|
| **文档** | AGENTS.md + docx/ 10 份 .md | docx/ 内容未与代码强制同步 |
| **后端检查** | Checkstyle (38 rules) + pom.xml 绑定 validate | `failOnViolation=false` — 仅警告不阻断 |
| **后端测试** | 1 个空壳 `ApplicationTests` | 零业务测试覆盖 |
| **前端检查** | `vue-tsc -b` (类型检查) | 无 ESLint、无 Prettier |
| **前端测试** | — | 零测试（无 Vitest/Jest） |
| **CI/CD** | — | 零自动化流水线 |
| **安全扫描** | — | 无 OWASP/Snyk/Dependabot |
| **Pre-commit** | — | 零 Git hooks |
| **构建缓存** | — | 全量重建 |

> 结论：**当前 Harness 处于「手工门禁」阶段** — 依赖开发者自觉运行 `pnpm build` 和 `mvn checkstyle:check`，无自动化强制链路。

---

## 🔴 致命缺口

### 1. 零测试覆盖

```
后端: src/test/ 仅 1 个空壳 ApplicationTests
前端: 无 Vitest/Jest/Cypress
```

**风险**：任何改动都可能引入回归，且无法感知。违反「闭环测试与反馈」原则。

### 2. 无 CI/CD 流水线

```
.gitignore:  不存在
CI configs:  零（无 GitHub Actions / GitLab CI / Jenkinsfile）
Git hooks:   零
```

**风险**：代码可在不通过任何检查的情况下提交和合并。

### 3. 前端零 Lint

```
ESLint:  无配置
Prettier: 无配置
```

**风险**：代码风格不统一，`vue-tsc` 仅检查类型不检查风格。

---

## 🟠 高优先级缺口

### 4. Checkstyle 仅警告

```xml
<failOnViolation>false</failOnViolation>
```
当前 38 条规则违反时只打印警告，构建继续。应改为 `true` 并在 CI 中阻断。

### 5. 无安全扫描

已知问题（硬编码密码、无认证端点）在 AGENTS.md 中记录了但无工具自动检测：
- 无 OWASP Dependency Check（CVE 扫描）
- 无 Secret 扫描（防止新密码硬编码）
- 无 SAST（SonarQube / SpotBugs）

### 6. 无构建缓存

`pnpm build` 每次全量 `vue-tsc -b && vite build`，无增量编译。后端 Maven 同样无缓存策略。

---

## 🟡 中优先级缺口

### 7. docx/ 与代码不同步

AGENTS.md R5 规定了文档同步规则，但无自动化检查：
- 新增 API 端点后，`api-routes.md` 是否更新？无法验证。
- 新增实体字段后，`entity-relationships.md` 是否更新？无法验证。

### 8. 无依赖更新管理

- 后端：无 Dependabot / Renovate
- 前端：无 `pnpm outdated` 集成

### 9. 无环境隔离

- 仅 `application.yml` / `application-prod.yml` 两个配置文件
- 无 dev/staging/prod 三环境分离
- 密码仍硬编码在配置文件中

---

## 理想 Harness 流水线

```
Developer Push
     │
     ▼
┌─ Pre-commit Hook ──────────────────────────────┐
│  • ESLint + Prettier (前端)                     │
│  • Checkstyle (后端)                             │
│  • Secret scan (detect-secrets / gitleaks)       │
└────────────────────────────────────────────────┘
     │ (通过)
     ▼
┌─ CI Pipeline (GitHub Actions) ─────────────────┐
│  1. Install deps (pnpm install / mvn)           │
│  2. Lint (ESLint + vue-tsc + Checkstyle)       │
│  3. Test (Vitest + JUnit)                       │
│  4. Security scan (OWASP + SAST)                │
│  5. Build (pnpm build / mvn package)            │
│  6. Doc drift check (API routes vs docx)        │
│  7. Deploy (if main)                            │
└────────────────────────────────────────────────┘
     │
     ▼
┌─ Feedback Loop ────────────────────────────────┐
│  • Test failures → 自动通知 + 阻断              │
│  • Lint violations → PR comment                │
│  • Security alerts → 紧急阻断                   │
│  • Doc drift → warning comment                 │
└────────────────────────────────────────────────┘
```

---

## 实施路线图

### Phase 1 — 止血（本周）

| # | 项 | 工作量 | 说明 |
|---|----|:--:|------|
| 1 | 创建 `.gitignore` | 10min | 排除 `node_modules/`, `target/`, `dist/`, `.env` |
| 2 | 前端 ESLint + Prettier | 30min | `eslint.config.js` + `.prettierrc` + `package.json` lint 脚本 |
| 3 | 后端 Checkstyle `failOnViolation=true` | 2min | 改一行 XML |
| 4 | Pre-commit hook | 20min | `lint-staged` + `husky` — 阻断未 lint 的提交 |

### Phase 2 — 加固（下周）

| # | 项 | 工作量 | 说明 |
|---|----|:--:|------|
| 5 | 后端单元测试骨架 | 2h | `AuthServiceTest`, `PageServiceTest` — 至少覆盖核心方法 |
| 6 | 前端 Vitest 配置 | 1h | `vitest.config.ts` + 组件渲染测试 |
| 7 | GitHub Actions CI | 1h | `.github/workflows/ci.yml` — lint → test → build |
| 8 | Secret scan | 30min | `gitleaks` 或 `detect-secrets` pre-commit |

### Phase 3 — 深化（本月）

| # | 项 | 工作量 | 说明 |
|---|----|:--:|------|
| 9 | OWASP Dependency Check | 1h | Maven + npm CVE 扫描 |
| 10 | 文档漂移检查 | 1h | 脚本对比 controller 端点 vs `api-routes.md` 是否一致 |
| 11 | Dependabot / Renovate | 30min | 自动 PR 更新依赖 |
| 12 | 环境变量外部化 | 1h | 移除 `application.yml` 中硬编码密码 |

---

## 当前最优度评分

| 维度 | 评分 | 说明 |
|------|:--:|------|
| 文档完整性 | 🟢 8/10 | AGENTS.md + docx/ 结构完善 |
| 代码规范检查 | 🟡 5/10 | Checkstyle 有但未强制，前端无 lint |
| 测试覆盖 | 🔴 0/10 | 零业务测试 |
| CI/CD 自动化 | 🔴 0/10 | 零流水线 |
| 安全扫描 | 🔴 0/10 | 零自动检查 |
| 提交门禁 | 🔴 0/10 | 无 pre-commit / CI gate |
| **综合** | 🔴 **2.2/10** | 有文档有规范，但无自动化强制 —

 **远未达到最优** |
