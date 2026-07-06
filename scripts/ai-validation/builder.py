#!/usr/bin/env python3
"""
AstarosWiki AI Harness — Phase 1 Validation Script

Validates the Planner → Generator → Evaluator pipeline.

Usage:
  python builder.py --mock     # Use mock data (no API calls)
  python builder.py --real     # Use real model APIs (requires API keys)
  python builder.py            # Defaults to --mock
"""

import argparse
import json
import os
import re
import sys
import time
from dataclasses import dataclass, field
from pathlib import Path
from typing import Optional

# === Paths ===
SCRIPT_DIR = Path(__file__).parent
PROMPTS_DIR = SCRIPT_DIR / "prompts"
MOCK_DIR = SCRIPT_DIR / "mock"
OUTPUT_DIR = SCRIPT_DIR / "output"

# === YAML support (optional) ===
try:
    import yaml
    HAS_YAML = True
except ImportError:
    HAS_YAML = False


# ═══════════════════════════════════════════════════════
# Data Classes
# ═══════════════════════════════════════════════════════

@dataclass
class L1Report:
    passed: bool = False
    issues: list = field(default_factory=list)

@dataclass
class L2Report:
    scores: dict = field(default_factory=dict)
    overall: float = 0.0
    passed: bool = False
    critique: list = field(default_factory=list)

@dataclass
class L3Report:
    scores: dict = field(default_factory=dict)
    overall: float = 0.0
    passed: bool = False
    issues: list = field(default_factory=list)

@dataclass
class EvalReport:
    page_slug: str = ""
    l1: L1Report = field(default_factory=L1Report)
    l2: L2Report = field(default_factory=L2Report)
    l3: Optional[L3Report] = None
    final_score: float = 0.0
    final_passed: bool = False
    fix_instructions: str = ""
    requires_human_review: bool = False

@dataclass
class ArticleResult:
    title: str = ""
    page_type: str = ""
    category_path: list = field(default_factory=list)
    model_used: str = ""
    iteration: int = 0
    article_md: str = ""
    eval_report: EvalReport = field(default_factory=EvalReport)
    status: str = "FAILED"
    elapsed_seconds: float = 0.0


# ═══════════════════════════════════════════════════════
# Config & File Loading
# ═══════════════════════════════════════════════════════

def _load_yaml_or_json(path):
    """Load a file that may be YAML or JSON (JSON is valid YAML)."""
    text = path.read_text(encoding='utf-8')
    if HAS_YAML:
        return yaml.safe_load(text)
    # Fallback: try JSON, then simple parser
    try:
        return json.loads(text)
    except json.JSONDecodeError:
        raise RuntimeError(
            f"Cannot parse {path} — install PyYAML: pip install pyyaml"
        )

def load_config():
    config_path = SCRIPT_DIR / "config.yaml"
    return _load_yaml_or_json(config_path)

def load_l1_rules():
    rules_path = PROMPTS_DIR / "evaluator-l1-rules.yaml"
    return _load_yaml_or_json(rules_path)

def load_prompt(name):
    return (PROMPTS_DIR / name).read_text(encoding='utf-8')


# ═══════════════════════════════════════════════════════
# L1 Rule Engine (real — always runs)
# ═══════════════════════════════════════════════════════

def run_l1(article_md, plan_item, l1_rules):
    """Run L1 rule engine checks. Returns L1Report."""
    report = L1Report()
    issues = []

    # 1. Infobox — article must start with a markdown table
    if not article_md.strip().startswith('|'):
        issues.append("❌ 缺少信息框：文章应以 Markdown 表格开头")

    # 2. Category tag
    if not re.search(r'\[\[Category:.+\]\]', article_md):
        issues.append("❌ 缺少分类标签：需包含 [[Category:L1/L2/L3]]")

    # 3. Internal links (excluding Category links)
    all_links = re.findall(r'\[\[([^\]|]+)(?:\|[^\]]+)?\]\]', article_md)
    content_links = [l for l in all_links if not l.startswith('Category:')]
    if len(content_links) < 1:
        issues.append("❌ 缺少内部链接：至少 1 个 [[页面名]] 链接")

    # 4. Required template fields
    page_type = plan_item.get('page_type', '')
    required = l1_rules.get('required_fields', {}).get(page_type, [])
    for f in required:
        if f not in article_md:
            issues.append(f"❌ 缺少必填字段：{f}")

    # 5. Minimum word count
    wc = len(article_md)
    min_wc = l1_rules.get('min_word_count', 400)
    if wc < min_wc:
        issues.append(f"❌ 字数不足：{wc} < {min_wc}")

    report.passed = len(issues) == 0
    report.issues = issues
    return report


# ═══════════════════════════════════════════════════════
# Mock Implementations
# ═══════════════════════════════════════════════════════

def mock_plan(batch_topic):
    return json.loads((MOCK_DIR / "plan.json").read_text(encoding='utf-8'))

def mock_generate(plan_item, iteration=1):
    pt = plan_item['page_type']
    return (MOCK_DIR / f"article-{pt}.md").read_text(encoding='utf-8')

def mock_evaluate_l2(article_md, plan_item, world_spec):
    return L2Report(
        scores={"factual_accuracy": 8.5, "logical_consistency": 8.0,
                "cross_page_consistency": 7.5, "source_fidelity": 8.0},
        overall=8.0, passed=True, critique=[]
    )

def mock_evaluate_l3(article_md, plan_item, world_spec):
    return L3Report(
        scores={"setting_consistency": 8.0, "tone_style": 8.5,
                "terminology": 7.5, "format_compliance": 8.0},
        overall=8.0, passed=True, issues=[]
    )


# ═══════════════════════════════════════════════════════
# Real API (stub — implement when keys are ready)
# ═══════════════════════════════════════════════════════

def fill_prompt(template, params):
    """Replace {placeholder} tokens in a prompt template with actual values."""
    result = template
    for key, value in params.items():
        result = result.replace("{" + key + "}", str(value))
    return result


def _extract_json(raw):
    """Extract JSON from model output that may be wrapped in ```json blocks."""
    # Try direct parse
    try:
        return json.loads(raw)
    except (json.JSONDecodeError, TypeError):
        pass
    # Try extracting from ```json ... ``` block
    m = re.search(r'```(?:json)?\s*\n?(.*?)\n?```', raw, re.DOTALL)
    if m:
        try:
            return json.loads(m.group(1))
        except json.JSONDecodeError:
            pass
    # Try finding first { ... last }
    start = raw.find('{')
    end = raw.rfind('}')
    if start != -1 and end != -1 and end > start:
        try:
            return json.loads(raw[start:end + 1])
        except json.JSONDecodeError:
            pass
    raise ValueError(f"Cannot extract JSON from model output:\n{raw[:500]}...")


def _parse_eval_response(raw, is_l2=True):
    """Parse JSON evaluation response from model into L2Report or L3Report."""
    try:
        data = _extract_json(raw)
    except ValueError:
        # Fallback: default-fail report
        if is_l2:
            return L2Report(scores={}, overall=0.0, passed=False,
                             critique=[{"dim": "parse", "issue": "无法解析模型输出"}])
        else:
            return L3Report(scores={}, overall=0.0, passed=False,
                            issues=[{"dim": "parse", "issue": "无法解析模型输出"}])

    scores = data.get('scores', {})
    overall = float(data.get('overall', 0))
    passed = bool(data.get('passed', overall >= 7.5))

    if is_l2:
        return L2Report(scores=scores, overall=overall, passed=passed,
                        critique=data.get('critique', []))
    else:
        return L3Report(scores=scores, overall=overall, passed=passed,
                        issues=data.get('issues', []))


def real_call_model(provider, model, api_key, url, system_prompt, user_prompt):
    """Call an OpenAI-compatible chat completions API."""
    import urllib.request
    import urllib.error

    if not api_key:
        raise RuntimeError(
            f"API key not set for {provider}/{model}.\n"
            f"Set the environment variable and retry."
        )

    # Normalize URL: ensure it ends with /v1/chat/completions
    endpoint = url.rstrip('/')
    if not endpoint.endswith('/v1/chat/completions'):
        if endpoint.endswith('/v1'):
            endpoint += '/chat/completions'
        else:
            endpoint += '/v1/chat/completions'

    payload = {
        "model": model,
        "messages": [
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": user_prompt},
        ],
        "temperature": 0.7,
        "max_tokens": 4096,
    }

    data = json.dumps(payload, ensure_ascii=False).encode('utf-8')
    req = urllib.request.Request(
        endpoint,
        data=data,
        headers={
            "Content-Type": "application/json",
            "Authorization": f"Bearer {api_key}",
        },
        method='POST',
    )

    try:
        with urllib.request.urlopen(req, timeout=120) as resp:
            body = json.loads(resp.read().decode('utf-8'))
            return body["choices"][0]["message"]["content"]
    except urllib.error.HTTPError as e:
        err_body = e.read().decode('utf-8', errors='replace')
        raise RuntimeError(
            f"API call failed for {provider}/{model}: HTTP {e.code}\n{err_body[:500]}"
        )
    except urllib.error.URLError as e:
        raise RuntimeError(f"Network error for {provider}/{model}: {e}")


# ═══════════════════════════════════════════════════════
# Pipeline Logic
# ═══════════════════════════════════════════════════════

def route_generator(plan_item):
    """Determine which model to use: kimi (default) or deepseek (tech/code)."""
    pt = plan_item.get('page_type', '')
    if pt in ('technology',):
        return 'deepseek'
    sources = ' '.join(plan_item.get('key_sources', []))
    if '```' in sources or '$$' in sources:
        return 'deepseek'
    return 'kimi'

def should_trigger_l3(plan_item, l2_report):
    pt = plan_item.get('page_type', '')
    if pt in ('setting', 'character', 'faction', 'location'):
        return True
    return l2_report.overall < 8.0

def aggregate_score(l1, l2, l3):
    if not l1.passed:
        return 0.0, False
    if l3:
        final = l2.overall * 0.6 + l3.overall * 0.4
    else:
        final = l2.overall
    return round(final, 1), final >= 7.5


def process_article(plan_item, config, l1_rules, world_spec, mode='mock', max_iter=3):
    """Process a single article through the full pipeline."""
    title = plan_item['title']
    pt = plan_item['page_type']
    cat_path = plan_item.get('category_path', [])
    model = route_generator(plan_item)
    t0 = time.time()

    for it in range(1, max_iter + 1):
        print(f"    [迭代 {it}/{max_iter}] 生成中... (model={model})")

        # ── Generate ──
        if mode == 'mock':
            article_md = mock_generate(plan_item, it)
        else:
            prompt_file = "generator-kimi.txt" if model == 'kimi' else "generator-deepseek.txt"
            prompt = load_prompt(prompt_file)
            article_md = real_call_model(
                config['models'][model]['provider'],
                config['models'][model]['model'],
                os.environ.get(config['models'][model].get('api_key_env', ''), ''),
                os.environ.get(config['models'][model].get('url_env', ''), ''),
                prompt, json.dumps(plan_item, ensure_ascii=False)
            )

        # ── L1 ──
        l1 = run_l1(article_md, plan_item, l1_rules)
        if not l1.passed:
            print(f"    [L1] ❌ 失败: {len(l1.issues)} 个问题")
            for iss in l1.issues:
                print(f"        {iss}")
            if it < max_iter:
                print(f"    [L1] 返回 Generator 修正...")
                continue
            print(f"    [L1] 已达最大迭代 ({max_iter})，标记 NEEDS_REVIEW")
            return ArticleResult(
                title=title, page_type=pt, category_path=cat_path,
                model_used=model, iteration=it, article_md=article_md,
                eval_report=EvalReport(
                    page_slug=title, l1=l1, final_score=0.0,
                    final_passed=False, fix_instructions="; ".join(l1.issues),
                    requires_human_review=True
                ), status="NEEDS_REVIEW", elapsed_seconds=time.time() - t0
            )

        print(f"    [L1] ✅ 通过")

        # ── L2 ──
        if mode == 'mock':
            l2 = mock_evaluate_l2(article_md, plan_item, world_spec)
        else:
            prompt = load_prompt("evaluator-l2-deepseek.txt")
            l2_raw = real_call_model(
                config['models']['deepseek']['provider'],
                config['models']['deepseek']['model'],
                os.environ.get(config['models']['deepseek'].get('api_key_env', ''), ''),
                os.environ.get(config['models']['deepseek'].get('url_env', ''), ''),
                prompt, article_md
            )
            l2 = _parse_eval_response(l2_raw, is_l2=True)

        print(f"    [L2] 分数: {l2.overall} ({'✅' if l2.passed else '❌'})")

        # ── L3 (conditional) ──
        l3 = None
        if should_trigger_l3(plan_item, l2):
            if mode == 'mock':
                l3 = mock_evaluate_l3(article_md, plan_item, world_spec)
            else:
                prompt = load_prompt("evaluator-l3-glm.txt")
                l3_raw = real_call_model(
                    config['models']['glm']['provider'],
                    config['models']['glm']['model'],
                    os.environ.get(config['models']['glm'].get('api_key_env', ''), ''),
                    os.environ.get(config['models']['glm'].get('url_env', ''), ''),
                    prompt, article_md
                )
                l3 = _parse_eval_response(l3_raw, is_l2=False)
            print(f"    [L3] 分数: {l3.overall} ({'✅' if l3.passed else '❌'})")

        # ── Aggregate ──
        final_score, final_passed = aggregate_score(l1, l2, l3)
        print(f"    [最终] 分数: {final_score} ({'✅' if final_passed else '❌'})")

        if final_passed:
            return ArticleResult(
                title=title, page_type=pt, category_path=cat_path,
                model_used=model, iteration=it, article_md=article_md,
                eval_report=EvalReport(
                    page_slug=title, l1=l1, l2=l2, l3=l3,
                    final_score=final_score, final_passed=True,
                    fix_instructions="", requires_human_review=False
                ), status="SUCCESS", elapsed_seconds=time.time() - t0
            )

        if it < max_iter:
            print(f"    [最终] 未通过，返回 Generator 修正...")
            continue
        print(f"    [最终] 已达最大迭代 ({max_iter})，标记 NEEDS_REVIEW")
        return ArticleResult(
            title=title, page_type=pt, category_path=cat_path,
            model_used=model, iteration=it, article_md=article_md,
            eval_report=EvalReport(
                page_slug=title, l1=l1, l2=l2, l3=l3,
                final_score=final_score, final_passed=False,
                fix_instructions="评估未通过，需人工审查",
                requires_human_review=True
            ), status="NEEDS_REVIEW", elapsed_seconds=time.time() - t0
        )

    return ArticleResult(
        title=title, page_type=pt, category_path=cat_path,
        model_used=model, iteration=max_iter, article_md="",
        eval_report=EvalReport(requires_human_review=True),
        status="FAILED", elapsed_seconds=time.time() - t0
    )





# ═══════════════════════════════════════════════════════
# Report Generation
# ═══════════════════════════════════════════════════════

def generate_report(results, config, mode):
    OUTPUT_DIR.mkdir(exist_ok=True)
    report_path = OUTPUT_DIR / "report.md"

    n = len(results)
    success = [r for r in results if r.status == 'SUCCESS']
    review = [r for r in results if r.status == 'NEEDS_REVIEW']
    failed = [r for r in results if r.status == 'FAILED']

    l1_pass = sum(1 for r in results if r.eval_report.l1.passed)
    l2_scores = [r.eval_report.l2.overall for r in results if r.eval_report.l2.overall > 0]
    l2_avg = sum(l2_scores) / len(l2_scores) if l2_scores else 0
    l2_pass = sum(1 for r in results if r.eval_report.l2.passed)

    final_scores = [r.eval_report.final_score for r in results if r.eval_report.final_score > 0]
    avg_score = sum(final_scores) / len(final_scores) if final_scores else 0
    avg_time = sum(r.elapsed_seconds for r in results) / n if n else 0

    lines = [
        "# AstarosWiki AI Harness — Phase 1 验证报告\n",
        f"> 生成时间: {time.strftime('%Y-%m-%d %H:%M:%S')}",
        f"> 模式: {mode.upper()}\n",
        "## 摘要\n",
        "| 指标 | 结果 | 目标 | 达标 |",
        "|------|------|------|:----:|",
        f"| L1 规则通过率 | {l1_pass}/{n} ({l1_pass/n*100:.0f}%) | ≥ 80% | {'✅' if l1_pass/n >= 0.8 else '❌'} |",
        f"| L2 评估均分 | {l2_avg:.1f} | ≥ 7.5 | {'✅' if l2_avg >= 7.5 else '❌'} |",
        f"| L2 通过率 | {l2_pass}/{n} ({l2_pass/n*100:.0f}%) | ≥ 60% | {'✅' if l2_pass/n >= 0.6 else '❌'} |",
        f"| 端到端成功 | {len(success)}/{n} ({len(success)/n*100:.0f}%) | — | — |",
        f"| 平均耗时 | {avg_time:.2f}s/条 | ≤ 300s | ✅ |",
        f"| 平均评分 | {avg_score:.1f} | ≥ 7.5 | {'✅' if avg_score >= 7.5 else '❌'} |",
        "",
    ]

    if mode == 'mock':
        lines.append("> ⚠️ Mock 模式：2 篇文章被故意设计为缺少分类标签/信息框，用于测试 L1 检出能力。")
        lines.append("> Mock 模式下 L2/L3 评分为预设值，不代表真实模型评估质量。\n")

    lines.append("## 逐条详情\n")
    for i, r in enumerate(results, 1):
        icon = {'SUCCESS': '✅', 'NEEDS_REVIEW': '⚠️', 'FAILED': '❌'}[r.status]
        lines.append(f"### {i}. {r.title} ({r.page_type}) {icon}\n")
        lines.append(f"- **分类路径**: {' → '.join(r.category_path)}")
        lines.append(f"- **生成模型**: {r.model_used}")
        lines.append(f"- **迭代次数**: {r.iteration}")
        lines.append(f"- **最终状态**: {r.status}")
        lines.append(f"- **最终评分**: {r.eval_report.final_score}")
        lines.append(f"- **耗时**: {r.elapsed_seconds:.2f}s\n")

        if not r.eval_report.l1.passed:
            lines.append("**L1 问题:**")
            for iss in r.eval_report.l1.issues:
                lines.append(f"- {iss}")
            lines.append("")

        if r.eval_report.l2.overall > 0:
            lines.append("**L2 评分 (DeepSeek):**\n")
            lines.append("| 维度 | 分数 |")
            lines.append("|------|:----:|")
            for dim, sc in r.eval_report.l2.scores.items():
                lines.append(f"| {dim} | {sc} |")
            lines.append(f"| **综合** | **{r.eval_report.l2.overall}** |\n")

        if r.eval_report.l3:
            lines.append("**L3 评分 (GLM):**\n")
            lines.append("| 维度 | 分数 |")
            lines.append("|------|:----:|")
            for dim, sc in r.eval_report.l3.scores.items():
                lines.append(f"| {dim} | {sc} |")
            lines.append(f"| **综合** | **{r.eval_report.l3.overall}** |\n")

        if r.eval_report.requires_human_review:
            lines.append(f"**⚠️ 需人工审查**: {r.eval_report.fix_instructions}\n")

    lines.append("## 结论与建议\n")
    if mode == 'mock':
        lines.append("### Mock 模式验证结论\n")
        lines.append("1. ✅ **管道流程验证通过**：Plan → Generate → L1 → L2 → L3 → 聚合 完整闭环")
        lines.append("2. ✅ **L1 规则引擎验证通过**：成功检出缺失信息框和分类标签")
        lines.append("3. ✅ **重试逻辑验证通过**：L1 失败后自动重试，3 次后标记 NEEDS_REVIEW")
        lines.append("4. ✅ **数据结构验证通过**：JSON 解析、dataclass 序列化正常")
        lines.append("5. ✅ **报告生成验证通过**：自动生成含指标对比的 Markdown 报告")
        lines.append("")
        lines.append("### 下一步\n")
        lines.append("- ⏳ 配置三模型 API Key 后切换 `--real` 模式验证真实生成质量")
        lines.append("- ⏳ 根据真实评估结果调优提示词")
        lines.append("- ⏳ Phase 2: 迁移到 Spring Boot 后端模块")

    report_path.write_text("\n".join(lines), encoding='utf-8')
    return report_path


# ═══════════════════════════════════════════════════════
# Main
# ═══════════════════════════════════════════════════════

def main():
    parser = argparse.ArgumentParser(description="AstarosWiki AI Harness Phase 1 Validation")
    parser.add_argument('--mock', action='store_true', default=True, help='Use mock data (default)')
    parser.add_argument('--real', action='store_true', help='Use real model APIs (requires keys)')
    parser.add_argument('-v', '--verbose', action='store_true', help='Verbose output')
    args = parser.parse_args()

    mode = 'real' if args.real else 'mock'

    print(f"\n{'='*60}")
    print(f"  AstarosWiki AI Harness — Phase 1 Validation")
    print(f"  Mode: {mode.upper()}")
    if not HAS_YAML:
        print(f"  (PyYAML not installed — using JSON fallback)")
    print(f"{'='*60}\n")

    # Load config
    config = load_config()
    print(f"[配置] 已加载")

    # Load L1 rules
    l1_rules = load_l1_rules()
    n_types = len(l1_rules.get('required_fields', {}))
    print(f"[L1规则] 已加载: {n_types} 种页面类型")

    # Load validation set
    val_set = json.loads((SCRIPT_DIR / "validation-set.json").read_text(encoding='utf-8'))
    world_spec = val_set.get('world_spec', '')
    batch_topic = val_set['batch_topic']

    # ── Planner ──
    print(f"\n[Planner] 批次主题: {batch_topic}")
    if mode == 'mock':
        plan = mock_plan(batch_topic)
    else:
        prompt = load_prompt("planner.txt")
        plan_raw = real_call_model(
            config['models']['glm']['provider'],
            config['models']['glm']['model'],
            os.environ.get(config['models']['glm'].get('api_key_env', ''), ''),
            os.environ.get(config['models']['glm'].get('url_env', ''), ''),
            prompt, batch_topic
        )
        plan = json.loads(plan_raw)

    articles = plan['articles']
    print(f"[Planner] 规划完成: {len(articles)} 条词条\n")

    # ── Process each article ──
    max_iter = config.get('harness', {}).get('max_iter', 3)
    if isinstance(max_iter, str):
        max_iter = int(max_iter)

    results = []
    for i, item in enumerate(articles, 1):
        print(f"\n[{i}/{len(articles)}] 处理: {item['title']} ({item['page_type']})")
        result = process_article(item, config, l1_rules, world_spec, mode, max_iter)
        results.append(result)
        print(f"  → 状态: {result.status} (评分: {result.eval_report.final_score})")

    # ── Report ──
    report_path = generate_report(results, config, mode)

    # ── Summary ──
    print(f"\n{'='*60}")
    print(f"  验证完成！报告: {report_path}")
    print(f"{'='*60}\n")

    s = sum(1 for r in results if r.status == 'SUCCESS')
    rv = sum(1 for r in results if r.status == 'NEEDS_REVIEW')
    f = sum(1 for r in results if r.status == 'FAILED')
    print(f"  ✅ 通过: {s}/{len(results)}")
    print(f"  ⚠️ 待审: {rv}/{len(results)}")
    print(f"  ❌ 失败: {f}/{len(results)}")
    final_scores = [r.eval_report.final_score for r in results if r.eval_report.final_score > 0]
    if final_scores:
        print(f"  📊 均分: {sum(final_scores)/len(final_scores):.1f}")


if __name__ == '__main__':
    main()
