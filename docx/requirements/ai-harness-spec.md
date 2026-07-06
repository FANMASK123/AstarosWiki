---
title: AI 建站 Harness 规格说明书
description: 三角色（Planner-Generator-Evaluator）AI 自动建站系统规格 — 角色分配、架构、数据流、API、提示词、评估管道、实施路线
domain: requirements
category: spec
status: draft
created: 2026-07-05
updated: 2026-07-05
version: 1.0.0
related_docs:
  - docx/requirements/category-spec-v3.md       # 分类三级封顶约束
  - docx/architecture/page-lifecycle.md          # 页面状态机 SAVED→DRAFT→PUBLISHED
  - docx/architecture/entity-relationships.md    # 数据库表结构
  - docx/api/api-routes.md                       # 现有端点清单
  - docx/requirements/harness-audit.md           # 工程 Harness 现状
  - AGENTS.md                                    # R1-R5 刚性规则
---

# AI 建站 Harness 规格说明书 v1.0

> 本文档是 AI 自动建站系统的**唯一真相源**。角色分配、架构、数据流、提示词引用、API 对接均以本文档为准。

---

## §0 术语与边界 — 两套 Harness 的区分

> ⚠️ 必须区分以下两套系统，不可混淆。

| 术语 | 含义 | 状态 |
|------|------|------|
| **MoA 对话 Harness** | 当前 Hermes 混合智能体（GLM-5.2 聚合器 + DeepSeek/Kimi 参考模型），用于**对话式分析决策** | 已运行 |
| **AI 建站 Harness** | 本文档定义的系统：三角色（Planner-Generator-Evaluator）**批量自动生成 Wiki 词条** | 待建设 |

**关键区别**：

| 维度 | MoA 对话 Harness | AI 建站 Harness |
|------|------------------|-----------------|
| 目的 | 对话分析、方案推演 | 批量产出词条内容并入库 |
| 参考模型权限 | 仅建议，无执行权 | Generator/Evaluator 需真正调用 API 产出 |
| 产出物 | 对话文本 | `wiki_pages` 表中的词条记录 |
| 触发方式 | 用户对话 | 前端按钮 / 后端批次任务 |
| 持久化 | 无 | MySQL |

**本文档仅描述「AI 建站 Harness」**。

---

## §1 角色分配与模型映射

### 1.1 终版角色分配

| 角色 | 默认模型 | 动态切换条件 | 核心职责 |
|:---:|:---:|---|---|
| **Planner** | GLM-5.2 | 外部记忆完善时可降级 Kimi（省成本） | 全局规划：分类树维护、批次任务拆解、验收标准定义 |
| **Generator** | Kimi-2.7 | 代码/公式/数据条目 → DeepSeek-V4 | 百科词条批量生成、Markdown 续写、信息框填充 |
| **Evaluator L2** | DeepSeek-V4 | — | 事实/逻辑/跨词条冲突零容忍校验 |
| **Evaluator L3** | GLM-5.2 | 仅设定/文风类条目触发 | 设定一致性、文风体例审查 |
| **Evaluator L1** | 规则脚本 | — | 格式/必填字段/死链/分类标签检查 |

### 1.2 分配理由（Wiki 建站场景）

| 模型 | 核心禀赋 | 匹配角色 |
|------|----------|----------|
| GLM-5.2 | 超长上下文、结构化输出稳定、全局统筹 | Planner + Evaluator L3 |
| Kimi-2.7 | 长文本续写、Markdown/wiki 语法、批量生成 | Generator（百科） |
| DeepSeek-V4 | 数理/事实/逻辑零容忍、批判性强 | Evaluator L2 + Generator（代码） |

### 1.3 不采纳的方案

| 方案 | 否决理由 |
|------|----------|
| Kimi 做 Planner | 上下文容量不足以承载全站素材规划 |
| DeepSeek 做 Generator | 推理强 ≠ 百科文本批量生成强，行文割裂 |
| GLM 做 Evaluator | 包容度高，容易漏检事实错误，Wiki 可信度受损 |

---

## §2 实施方案选择

### 2.1 三方案对比

| 维度 | A. Spring Boot 管道 | B. Hermes 工具代理 | C. Python 脚本 |
|------|:---:|:---:|:---:|
| 生产可用 | ✅ | ⚠️ | ❌ |
| 前端联动 | ✅ | ❌ | ❌ |
| 持久化 | ✅ MySQL | ❌ | ⚠️ 文件 |
| 开发周期 | 2-3 天 | 1 天 | 半天 |
| 侵入性 | 🟡 后端新增模块 | 🟢 零侵入 | 🟢 零侵入 |

### 2.2 终版选择：A 基座 + D 增强

```
方案 D = 方案 A（工程基座）
       + 架构层增强：事件驱动 Agent Bus（Phase 4+）
       + 能力层增强：外部记忆 / 规则 L1 / 动态路由 / 分层评估 / 缓存复用
```

### 2.3 螺旋落地路线

| 阶段 | 时间 | 产出 | 状态 |
|:---:|:---:|---|:---:|
| Phase 0 | 2h | 本规格文档 | 🔄 进行中 |
| Phase 1 | 4h | Python 脚本验证提示词（5 条典型词条） | ⏳ 待批准 |
| Phase 2 | 2-3 天 | Spring Boot AI 模块 + 基础管道 | ⏳ |
| Phase 3 | 1 天 | 前端触发 + 异步队列 + 状态机 | ⏳ |
| Phase 4 | 持续 | 叠加 D 增强：外部记忆→规则L1→共识→Agent Bus | ⏳ |

---

## §3 总体架构

```
用户 / Vue 前端
    │
    ▼
┌──────────────────────────────────────────────────┐
│  Spring Boot 后端（方案 A 基座）                    │
│  ──────────────────────────────────────────────  │
│                                                  │
│  WikiBuildOrchestrator（总控编排）                  │
│  ├─ PlannerService (GLM-5.2)                     │
│  ├─ GeneratorService                              │
│  │   ├─ Router → Kimi 2.7（百科）/ DeepSeek（代码）│
│  │   └─ 双Generator共识（争议词条，Phase 4+）       │
│  ├─ EvaluatorService                              │
│  │   ├─ L1 规则引擎（本地脚本）                    │
│  │   ├─ L2 DeepSeek（事实/逻辑）    ← 60%权重      │
│  │   ├─ L3 GLM（设定/文风）         ← 40%权重      │
│  │   └─ L4 人工抽审（5%阈值触发）                  │
│  └─ Controller Agent（超时/重试/降级/死信）        │
│                                                  │
│  外部记忆层（Phase 4+）                            │
│  ├─ 分类树索引                                    │
│  ├─ 世界设定 spec 向量库                          │
│  ├─ 已有词条关联图                                │
│  └─ 生成模板库                                    │
│                                                  │
│  工程能力                                         │
│  ├─ 状态机 + 异步队列 + 重试 + 人工审核队列         │
│  ├─ 批次上下文控制（不全站加载）                    │
│  ├─ 评估退出条件（max 3 次 + 阈值 7.5）            │
│  └─ Dashboard 可观测（阶段/重试/均分/成本）        │
└──────────────────────────────────────────────────┘
    │
    ▼
MySQL (wiki_pages + ai_build_jobs) + Vue 前端
```

---

## §4 数据流与状态机对接

### 4.1 AI 生成词条的生命周期

> AI 产出的词条**默认进入 `SAVED` 状态**，走现有审核流，**不自动发布**。

```
Planner 生成 plan.json
    │
    ▼
Generator 产出 article.md
    │
    ▼  调用 POST /api/ai/generate（内部调 createPage, publish=false）
    │
    ▼  status = SAVED（草稿，未提交审核）
    │
    ▼  Evaluator 评估
    │
    ├─ 通过 → 等待人工审核或显式发布
    └─ 不通过 → 返回 Generator 修正（max 3 次）
                  │
                  ├─ 3 次内修正通过 → SAVED
                  └─ 3 次仍不通过 → 标记 needs_human_review
```

### 4.2 与现有页面状态机对接

| AI 操作 | 对应状态 | 触发方式 |
|---------|----------|----------|
| Generator 首次产出 | `SAVED` | `createPage(publish=false)` |
| Generator 修正重生成 | 覆盖 `draftContent` | `updatePage(publish=false)` |
| 评估通过后提交审核 | `DRAFT` | `updatePage(publish=true)` |
| 管理员审核通过 | `PUBLISHED` | `approvePage()` |

> 参考 [`page-lifecycle.md`](../architecture/page-lifecycle.md) §状态机。

### 4.3 批次任务状态机

```
PENDING → PLANNING → GENERATING → EVALUATING → COMPLETED
                │          │           │
                ▼          ▼           ▼
             PLAN_FAIL  GEN_FAIL   EVAL_FAIL
                │          │           │
                └──────────┴───────────┘
                          ▼
                     RETRY (max 3)
                          │
                          ▼  仍失败
                     NEEDS_HUMAN_REVIEW
```

---

## §5 API 端点清单

> 新增端点，与 [`api-routes.md`](../api/api-routes.md) 同步。全部需 `ADMIN` 认证。

| 方法 | 路径 | 请求体 | 返回 | 说明 |
|------|------|--------|------|------|
| `POST` | `/api/ai/plan` | `{ batchTopic, categoryId?, requirements? }` | `AIPlanDTO` | 提交批次主题 → 返回规划 |
| `POST` | `/api/ai/generate` | `{ planItemId, routeTo? }` | `PageDTO` | 生成单条词条，`status=SAVED` |
| `POST` | `/api/ai/evaluate` | `{ pageSlug, layers: ["L1","L2","L3"] }` | `EvalReportDTO` | 分层评估 |
| `POST` | `/api/ai/build-batch` | `{ batchTopic, maxIter?, categoryId? }` | `{ jobId }` | 一键全流程，异步 |
| `GET` | `/api/ai/build-status/{jobId}` | — | `AIBuildJobDTO` | 查询批次状态 |
| `GET` | `/api/ai/jobs` | `?page=&size=&status=` | `PageResult<AIBuildJobDTO>` | 任务列表 |
| `POST` | `/api/ai/jobs/{id}/retry` | — | `AIBuildJobDTO` | 重试失败任务 |
| `POST` | `/api/ai/jobs/{id}/cancel` | — | `Void` | 取消任务 |

### 请求/响应格式

```typescript
// Planner 输出
interface AIPlanDTO {
  jobId: number
  batchGoal: string
  articles: AIPlanItem[]
  templates: Record<string, string[]>
  crossLinks: { from: string; to: string; type: string }[]
  acceptanceCriteria: Record<string, string>
  riskNotes: string[]
}

interface AIPlanItem {
  title: string
  categoryPath: string[]    // 必须遵守三级封顶
  pageType: 'character' | 'location' | 'event' | 'technology' | 'setting' | 'faction'
  templateFields: string[]
  keySources: string[]
  relatedPages: string[]
  priority: 'high' | 'medium' | 'low'
}

// Evaluator 输出
interface EvalReportDTO {
  pageSlug: string
  l1Report: { passed: boolean; issues: string[] }
  l2Report: { scores: Record<string, number>; overall: number; passed: boolean; critique: any[] }
  l3Report?: { scores: Record<string, number>; overall: number; passed: boolean; issues: any[] }
  finalScore: number
  finalPassed: boolean
  fixInstructions: string
  requiresHumanReview: boolean
}

// 批次任务
interface AIBuildJobDTO {
  id: number
  batchTopic: string
  status: 'PENDING' | 'PLANNING' | 'GENERATING' | 'EVALUATING' | 'COMPLETED' | 'FAILED' | 'NEEDS_HUMAN_REVIEW'
  totalArticles: number
  completedArticles: number
  failedArticles: number
  avgScore: number
  totalCost: number       // token 成本估算
  createdAt: string
  updatedAt: string
}
```

---

## §6 数据模型变更

### 6.1 新增表：`ai_build_jobs`

| 列 | 类型 | 约束 | 说明 |
|----|------|------|------|
| `id` | `BIGINT` | PK, AUTO | |
| `batch_topic` | `VARCHAR(500)` | NOT NULL | 批次主题 |
| `status` | `VARCHAR(50)` | NOT NULL | 任务状态 |
| `plan_json` | `LONGTEXT` | | Planner 输出 |
| `total_articles` | `INT` | default 0 | 计划词条数 |
| `completed_articles` | `INT` | default 0 | 已完成数 |
| `failed_articles` | `INT` | default 0 | 失败数 |
| `avg_score` | `DECIMAL(3,1)` | | 平均评估分 |
| `total_cost` | `DECIMAL(10,4)` | | token 成本估算 |
| `triggered_by` | `BIGINT` | FK → users.id | 触发者 |
| `created_at` | `DATETIME` | | |
| `updated_at` | `DATETIME` | | |

### 6.2 新增表：`ai_generation_logs`

| 列 | 类型 | 约束 | 说明 |
|----|------|------|------|
| `id` | `BIGINT` | PK, AUTO | |
| `job_id` | `BIGINT` | FK → ai_build_jobs.id | |
| `page_slug` | `VARCHAR(200)` | | 关联产出页面 |
| `plan_item_json` | `LONGTEXT` | | 该条目的规划 |
| `generated_content` | `LONGTEXT` | | Generator 产出 |
| `eval_report_json` | `LONGTEXT` | | Evaluator 报告 |
| `model_used` | `VARCHAR(50)` | | kimi / deepseek / glm |
| `role` | `VARCHAR(20)` | | planner / generator / evaluator |
| `iteration` | `INT` | default 1 | 修正迭代次数 |
| `final_score` | `DECIMAL(3,1)` | | |
| `status` | `VARCHAR(50)` | | SUCCESS / FAILED / NEEDS_REVIEW |
| `token_input` | `INT` | | 输入 token |
| `token_output` | `INT` | | 输出 token |
| `created_at` | `DATETIME` | | |

### 6.3 现有表不变更

> `wiki_pages`、`page_versions`、`categories` 等表**不新增字段**。AI 产出的词条通过现有 `createPage` API 入库，与人工创建的词条无差异。

---

## §7 提示词目录结构

> 提示词外部化到独立目录，不硬编码在 Java 代码中。便于迭代调优。

```
AstarosWiki/
└── config/ai-prompts/
    ├── planner.txt                    # Planner (GLM-5.2)
    ├── generator-router.txt           # 生成路由判断
    ├── generator-kimi.txt             # Generator 百科文本 (Kimi)
    ├── generator-deepseek.txt         # Generator 技术条目 (DeepSeek)
    ├── evaluator-l1-rules.yaml        # L1 规则引擎配置
    ├── evaluator-l2-deepseek.txt      # L2 事实/逻辑 (DeepSeek)
    ├── evaluator-l3-glm.txt           # L3 设定/文风 (GLM)
    ├── evaluator-aggregator.txt       # 多层评估聚合 (GLM)
    └── templates/
        ├── character.md               # 人物模板
        ├── location.md                # 地点模板
        ├── event.md                   # 事件模板
        ├── technology.md              # 技术模板
        ├── setting.md                 # 设定模板
        └── faction.md                 # 阵营模板
```

**Phase 1（Python 验证）**：提示词放在项目根 `prompts/` 目录，Python 脚本直接读取。

**Phase 2+（Spring Boot）**：提示词放在 `config/ai-prompts/`，Spring Boot 启动时读取，缓存到内存，支持热更新（监听文件变更）。

**版本管理**：提示词文件纳入 Git，每次修改提交 message 标注 `prompt: <role> <change>`。

---

## §8 外部记忆设计（Phase 4+）

> 降低对模型原生上下文的绝对依赖。

| 记忆层 | 内容 | 用途 |
|--------|------|------|
| 分类树索引 | `categories` 表全量缓存 | Planner 规划时读取，确保三级封顶 |
| 世界设定 spec | `docx/requirements/` + 核心设定文档 | Generator/Evaluator 参照 |
| 已有词条关联图 | `wiki_pages` 标题 + `[[内部链接]]` 解析 | 跨词条一致性检查 |
| 生成模板库 | `config/ai-prompts/templates/` | Generator 填充模板 |
| Corrective RAG | `ai_generation_logs` 中的修正对 | 越用越准（Phase 4+） |

**批次上下文注入范围**：

```
Planner 输入 = 当前批次主题
              + 相关分类分支（非全站）
              + 已有词条标题列表（非全文）
              + 世界设定核心规则摘要（非全文）
              + 用户特殊要求

最大 token 控制：≤ 32K input（GLM-5.2）
```

---

## §9 评估管道与退出条件

### 9.1 四层评估管道

```
Generator 产出
    │
    ▼
L1: 规则引擎（本地脚本，无模型调用）
    ├─ Wiki 语法检查（[[链接]]、信息框表格、分类标签）
    ├─ 必填字段检查（templateFields 全部填充）
    ├─ 死链检测（[[链接]] 目标是否存在）
    └─ 分类标签格式（[[Category:L1/L2/L3]]）
    │
    ├─ 通过 → L2
    └─ 不通过 → 直接返回 Generator 修正
    │
    ▼
L2: DeepSeek-V4（事实/逻辑）
    ├─ factual_accuracy（事实准确）
    ├─ logical_consistency（无自相矛盾）
    ├─ cross_page_consistency（与已有词条无冲突）
    └─ source_fidelity（仅基于素材，无幻觉）
    │
    ▼
L3: GLM-5.2（设定/文风，条件触发）
    触发条件：pageType in [setting, character, faction, location] OR L2 score < 8
    ├─ setting_consistency（与世界观核心设定一致）
    ├─ tone_style（百科客观语气，无口语）
    ├─ terminology（使用本站术语体系）
    └─ format_compliance（信息框/分类标签/内部链接）
    │
    ▼
L4: 人工抽审
    触发条件：L2 发现 major 冲突 OR 连续 2 次迭代同一问题未解决
    初期人工抽审比例：20%（稳定后降至 5%）
```

### 9.2 评分聚合规则

```
final_score = L1_passed ? (
    L2_score * 0.6 + (L3_triggered ? L3_score * 0.4 : L2_score * 0.4)
) : 0

passed = final_score >= 7.5 AND L1_passed
```

### 9.3 退出条件（防死循环）

| 参数 | 值 | 说明 |
|------|------|------|
| `max_iter` | **3** | Generator 最多重生成 3 次 |
| `threshold` | **7.5** | 评估通过阈值（10 分制） |
| `consecutive_same_issue` | **2** | 连续 2 次同一问题 → 强制人工审 |

> 3 次迭代后仍未通过 → `status = NEEDS_HUMAN_REVIEW`，不再自动重试。

### 9.4 Phase 1 验证成功指标

| 指标 | 目标 | 说明 |
|------|------|------|
| L1 规则通过率 | ≥ 4/5 (80%) | 5 条样本中至少 4 条通过格式检查 |
| L2 评估均分 | ≥ 7.5 | 事实/逻辑维度平均分 |
| L2 通过率 | ≥ 3/5 (60%) | 至少 3 条达到阈值 |
| 人工抽审 | 100% (初期) | Phase 1 全量人工审，确认质量基线 |
| 端到端耗时 | ≤ 5 分钟/条 | 含规划 + 生成 + 评估 |

---

## §10 成本/安全/Token 控制

### 10.1 成本控制

| 策略 | 说明 |
|------|------|
| L1 规则引擎前置 | 过滤 70% 格式问题，减少模型调用 |
| 批次上下文控制 | Planner 只加载当前批次相关分支，不全站喂 |
| Generator 路由 | 百科→Kimi（性价比高），仅代码条目→DeepSeek |
| 缓存复用 Plan | 同类批次复用规划模板，减少 Planner 调用 |
| Token 上限 | Planner input ≤ 32K；Generator output ≤ 4K/条（可配置，长词条可分段生成） |

### 10.2 安全（AGENTS.md R1 合规）

| 红线 | 说明 |
|------|------|
| API Key 外部化 | `application.yml` 用 `${AI_GLM_KEY}` / `${AI_KIMI_KEY}` / `${AI_DEEPSEEK_KEY}` 占位符 |
| 不自动发布 | AI 产出默认 `status = SAVED`，需人工审核 |
| 端点认证 | `/api/ai/*` 全部需 `ADMIN` 角色 |
| 不硬编码密码 | 禁止在提示词/代码中出现明文密钥 |
| 日志脱敏 | `ai_generation_logs` 不记录 API Key，只记录模型名 |

### 10.3 三模型 API 配置

| 本文档短名 | 实际 Provider 全限定名 | 调用时使用 |
|---------|-----------------|-----------|
| GLM-5.2 | `alibaba-coding-plan:glm-5.2` | Planner + Evaluator L3 |
| Kimi-2.7 | `kimi-coding-cn:kimi-k2.7-code` | Generator（百科） |
| DeepSeek-V4 | `deepseek:deepseek-v4-pro` | Evaluator L2 + Generator（代码） |

> 短名仅用于本文档可读性，实际 API 调用时使用右列全限定名。

```yaml
# application.yml（示例，实际用环境变量）
ai:
  models:
    glm:
      url: ${AI_GLM_URL:https://...}
      api-key: ${AI_GLM_KEY}
      model: glm-5.2
    kimi:
      url: ${AI_KIMI_URL:https://...}
      api-key: ${AI_KIMI_KEY}
      model: kimi-k2.7
    deepseek:
      url: ${AI_DEEPSEEK_URL:https://...}
      api-key: ${AI_DEEPSEEK_KEY}
      model: deepseek-v4
  harness:
    max-iter: 3
    threshold: 7.5
    planner-max-input-tokens: 32000
    generator-max-output-tokens: 4000
```

---

## §11 分类系统约束（category-spec-v3.md 对接）

> AI 生成的词条**必须遵守** [`category-spec-v3.md`](category-spec-v3.md) 的全部规则。

| 约束 | 规则 | AI 执行方式 |
|------|------|-------------|
| 三级封顶 | 不允许四级分类 | Planner 输出 `categoryPath` 最多 3 层 |
| 分类路径有效 | 只能选择已存在的分类 | Planner 从 `categories` 表读取，不编造 |
| 分类标签格式 | `[[Category:L1/L2/L3]]` | Generator 模板硬编码 |
| L3 不可导航 | L3 是终点 | Planner 不为 L3 生成子分类 |

---

## §12 前端兼容说明

### 12.1 当前编辑器现状

`PageEdit.vue` 使用纯 `<el-input type="textarea">`，**无 Markdown 编辑器/预览**。

### 12.2 AI 产出的兼容策略

| 策略 | 说明 |
|------|------|
| 产出格式 | 纯 Markdown 文本，写入 `content` 字段 |
| 入库方式 | 调用现有 `createPage({ title, content, categoryId, tags, publish: false })` |
| 状态 | `SAVED`（草稿），不自动发布 |
| 前端展示 | 编辑器升级前，`PageEdit.vue` 可直接编辑 AI 产出的 Markdown 文本 |
| 渲染 | `PageDetail.vue` 需后续接入 Markdown 渲染器（`markdown-it`），与 AI Harness 无关 |

> **AI Harness 不依赖前端 Markdown 渲染器上线。** 产出的是纯文本 Markdown，存入 `content` 字段，与人工输入无异。

---

## §13 文档漂移记录

> ⚠️ 探索中发现以下文档与实际代码不一致，记录于此供后续修正。

| 文档 | 描述 | 实际 | 差距 |
|------|------|------|------|
| `harness-audit.md` | 前端无 ESLint/Prettier | `package.json` 已含 ESLint 10 + Prettier 3.9 + lint-staged | ⚠️ 依赖已就位，配置状态待核实 |
| `harness-audit.md` | 前端零测试 | `package.json` 已含 Vitest 4 + @vue/test-utils | ⚠️ 依赖已就位，测试用例待补 |
| `harness-audit.md` | 无 pre-commit | `package.json` 已含 husky 9 + `prepare: husky` | ⚠️ 依赖已就位，hook 脚本待核实 |
| `harness-audit.md` | Checkstyle `failOnViolation=false` | `pom.xml` 已改为 `true` | ⚠️ 配置已改，CI 集成待核实 |
| `harness-audit.md` | 无 OWASP | `pom.xml` 已含 dependency-check-maven 11.1.1 | ⚠️ 依赖已就位，suppressions 待核实 |
| `entity-relationships.md` | 密码硬编码 `123456` | 待确认 `application.yml` 是否已外部化 | ⚠️ 待核实 |

> `harness-audit.md` 的"综合评分 2.2/10"已过时——依赖已基本就位，但配置生效与否需实际运行验证。后续应更新该文档。

---

## §14 实施检查清单

| # | 检查项 | Phase | 状态 |
|---|--------|:---:|:---:|
| 1 | spec 文档撰写完成 | 0 | ✅ |
| 2 | AGENTS.md 导航更新 | 0 | ✅ |
| 3 | 用户批准 spec | 0 | ⏳ |
| 4 | 三模型 API Key 就绪 | 1 | ⏳ |
| 5 | Python 验证脚本 | 1 | ⏳ |
| 6 | 5 条典型词条验证集 | 1 | ⏳ |
| 7 | 提示词定版 | 1 | ⏳ |
| 8 | Spring Boot AI 模块 | 2 | ⏳ |
| 9 | 异步队列 + 状态机 | 2 | ⏳ |
| 10 | 前端触发页面 | 3 | ⏳ |
| 11 | 外部记忆层 | 4 | ⏳ |
| 12 | L1 规则引擎 | 4 | ⏳ |
| 13 | Corrective RAG | 4+ | ⏳ |

---

## §15 约束总结

> 违反以下任一条的 AI 产出将被拒绝入库。

| # | 约束 | 来源 |
|---|------|------|
| C1 | AI 产出默认 `status = SAVED`，不自动发布 | 本规格 §4.1 |
| C2 | 分类路径最多三级，只能选择已存在分类 | category-spec-v3.md §1 |
| C3 | 评估最大迭代 3 次，阈值 7.5 | 本规格 §9.3 |
| C4 | API Key 用 `${ENV_VAR}` 占位符，不硬编码 | AGENTS.md R1 |
| C5 | `/api/ai/*` 端点全部需 `ADMIN` 认证 | AGENTS.md R1 |
| C6 | 提示词外部化到 `config/ai-prompts/`，不硬编码 | 本规格 §7 |
| C7 | 批次上下文 ≤ 32K input，不全站加载 | 本规格 §8 |
| C8 | `ai_generation_logs` 不记录 API Key | 本规格 §10.2 |
