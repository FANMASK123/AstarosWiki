# AGENTS.md — AstarosWiki

> 刚性规则 + `docx/` 导航。细节一律在 docx/ 中。

## 刚性需求与规则

> 违反任一条的代码将被拒绝。

### R1 — 安全边界

- 禁止修改 `SecurityConfig.java` 权限规则，除非用户明确授权
- 禁止硬编码密码/密钥/Token — 必须用 `${ENV_VAR}` 占位符
- 禁止新增无认证的敏感操作端点
- 禁止 `v-html` / `innerHTML` 渲染未消毒的用户输入

### R2 — 设计一致性

- 所有颜色用 CSS 变量（`var(--*)`），禁止硬编码
- 所有按钮 → `btn` / `btn-primary` / `btn-ghost`
- 所有卡片 → `card`
- 暗色/亮色不写独立样式，依赖 `[data-theme]`

### R3 — 质量门禁

- 每次改动后 `pnpm build`，零错误才通过
- 禁止 `any` 类型的导出函数签名
- 所有 `catch` 块至少 `console.error`
- API 响应必须 `res.code === 200` 再读 `res.data`
- 构建报错用三要素回复：❶ 什么错了 ❷ 为什么+怎么改(before/after) ❸ docx/ 文档链接

### R4 — 架构约束

- 后端：Controller → Service → Repository，Controller 不写业务逻辑
- 前端：组件不直接调 `axios` → 走 `src/api/`
- 状态：跨组件共享 → Pinia Store，不深层传递 props
- 不引入新范式（库/框架/构建工具）除非用户批准

### R5 — 文档同步

| 变更 | 更新 |
|------|------|
| 新增 API | `docx/api/api-routes.md` |
| 新增实体/字段 | `docx/architecture/entity-relationships.md` |
| 状态机变更 | `docx/architecture/page-lifecycle.md` |
| CSS/类变更 | `docx/design/design-system.md` |
| 前端模式变更 | `docx/frontend/frontend-patterns.md` |

## docx/ 导航

| 问题 | 文档 |
|------|------|
| 前端 lint 报错？ | `docx/frontend/frontend-patterns.md` § 反模式 |
| API 端点？ | `docx/api/api-routes.md` |
| JWT/权限/认证流程？ | `docx/architecture/auth-flow.md` |
| 页面状态 SAVED→PUBLISHED？ | `docx/architecture/page-lifecycle.md` |
| 数据库表结构？ | `docx/architecture/entity-relationships.md` |
| 数据流/请求链路？ | `docx/architecture/data-flow.md` |
| CSS 变量/组件类名？ | `docx/design/design-system.md` |
| 功能缺口？ | `docx/requirements/gap-analysis.md` |
| Wiki 设计需求？ | `docx/requirements/wiki-requirements.md` |
| Harness 流程？ | `docx/requirements/harness-audit.md` |
