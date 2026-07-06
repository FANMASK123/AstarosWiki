# AstarosWiki AI Harness — Phase 1 验证报告

> 生成时间: 2026-07-05 14:23:50
> 模式: MOCK

## 摘要

| 指标 | 结果 | 目标 | 达标 |
|------|------|------|:----:|
| L1 规则通过率 | 3/5 (60%) | ≥ 80% | ❌ |
| L2 评估均分 | 8.0 | ≥ 7.5 | ✅ |
| L2 通过率 | 3/5 (60%) | ≥ 60% | ✅ |
| 端到端成功 | 3/5 (60%) | — | — |
| 平均耗时 | 0.00s/条 | ≤ 300s | ✅ |
| 平均评分 | 8.0 | ≥ 7.5 | ✅ |

> ⚠️ Mock 模式：2 篇文章被故意设计为缺少分类标签/信息框，用于测试 L1 检出能力。
> Mock 模式下 L2/L3 评分为预设值，不代表真实模型评估质量。

## 逐条详情

### 1. 阿斯塔洛斯 (setting) ✅

- **分类路径**: 世界观 → 核心设定
- **生成模型**: kimi
- **迭代次数**: 1
- **最终状态**: SUCCESS
- **最终评分**: 8.0
- **耗时**: 0.00s

**L2 评分 (DeepSeek):**

| 维度 | 分数 |
|------|:----:|
| factual_accuracy | 8.5 |
| logical_consistency | 8.0 |
| cross_page_consistency | 7.5 |
| source_fidelity | 8.0 |
| **综合** | **8.0** |

**L3 评分 (GLM):**

| 维度 | 分数 |
|------|:----:|
| setting_consistency | 8.0 |
| tone_style | 8.5 |
| terminology | 7.5 |
| format_compliance | 8.0 |
| **综合** | **8.0** |

### 2. 永夜之地 (location) ✅

- **分类路径**: 地理 → 区域
- **生成模型**: kimi
- **迭代次数**: 1
- **最终状态**: SUCCESS
- **最终评分**: 8.0
- **耗时**: 0.00s

**L2 评分 (DeepSeek):**

| 维度 | 分数 |
|------|:----:|
| factual_accuracy | 8.5 |
| logical_consistency | 8.0 |
| cross_page_consistency | 7.5 |
| source_fidelity | 8.0 |
| **综合** | **8.0** |

**L3 评分 (GLM):**

| 维度 | 分数 |
|------|:----:|
| setting_consistency | 8.0 |
| tone_style | 8.5 |
| terminology | 7.5 |
| format_compliance | 8.0 |
| **综合** | **8.0** |

### 3. 虚空法师团 (faction) ⚠️

- **分类路径**: 阵营 → 法师组织
- **生成模型**: kimi
- **迭代次数**: 3
- **最终状态**: NEEDS_REVIEW
- **最终评分**: 0.0
- **耗时**: 0.00s

**L1 问题:**
- ❌ 缺少分类标签：需包含 [[Category:L1/L2/L3]]

**⚠️ 需人工审查**: ❌ 缺少分类标签：需包含 [[Category:L1/L2/L3]]

### 4. 第一次裂变战争 (event) ⚠️

- **分类路径**: 历史 → 重大事件
- **生成模型**: kimi
- **迭代次数**: 3
- **最终状态**: NEEDS_REVIEW
- **最终评分**: 0.0
- **耗时**: 0.00s

**L1 问题:**
- ❌ 缺少信息框：文章应以 Markdown 表格开头

**⚠️ 需人工审查**: ❌ 缺少信息框：文章应以 Markdown 表格开头

### 5. 符文锻造术 (technology) ✅

- **分类路径**: 技术 → 符文术
- **生成模型**: deepseek
- **迭代次数**: 1
- **最终状态**: SUCCESS
- **最终评分**: 8.0
- **耗时**: 0.00s

**L2 评分 (DeepSeek):**

| 维度 | 分数 |
|------|:----:|
| factual_accuracy | 8.5 |
| logical_consistency | 8.0 |
| cross_page_consistency | 7.5 |
| source_fidelity | 8.0 |
| **综合** | **8.0** |

## 结论与建议

### Mock 模式验证结论

1. ✅ **管道流程验证通过**：Plan → Generate → L1 → L2 → L3 → 聚合 完整闭环
2. ✅ **L1 规则引擎验证通过**：成功检出缺失信息框和分类标签
3. ✅ **重试逻辑验证通过**：L1 失败后自动重试，3 次后标记 NEEDS_REVIEW
4. ✅ **数据结构验证通过**：JSON 解析、dataclass 序列化正常
5. ✅ **报告生成验证通过**：自动生成含指标对比的 Markdown 报告

### 下一步

- ⏳ 配置三模型 API Key 后切换 `--real` 模式验证真实生成质量
- ⏳ 根据真实评估结果调优提示词
- ⏳ Phase 2: 迁移到 Spring Boot 后端模块