#!/usr/bin/env python3
"""文档漂移检查：对比 Controller 端点和 api-routes.md 是否同步。"""
import re, sys
from pathlib import Path

BASE = Path(__file__).resolve().parent.parent
CONTROLLERS = BASE / "AstarosWiki/src/main/java/com/example/astaroswiki/controller"
DOC = BASE / "docx/api/api-routes.md"

def normalize(path: str) -> str:
    """归一化参数占位符为 {param} 以便比对。"""
    return re.sub(r"\{[^}]+\}", "{param}", path)

# 1. Extract real endpoints from controllers
real = {}
for f in CONTROLLERS.glob("*.java"):
    code = f.read_text(encoding="utf-8")
    base_match = re.search(r'@RequestMapping\("([^"]+)"\)', code)
    base = base_match.group(1) if base_match else ""
    for m in re.finditer(r'@(Get|Post|Put|Delete)Mapping(?:\(("([^"]*)")?\))?', code):
        path = m.group(3) or ""
        full = f"{base}{path}"
        real[normalize(full)] = f"{m.group(1).upper()} {full}"

# 2. Extract documented endpoints from api-routes.md
doc = {}
if DOC.exists():
    for line in DOC.read_text(encoding="utf-8").splitlines():
        m = re.match(r"\| `(GET|POST|PUT|DELETE)` \| `([^`]+)`", line)
        if m:
            doc[normalize(m.group(2))] = f"{m.group(1)} {m.group(2)}"

# 3. Compare
only_real = {k: v for k, v in real.items() if k not in doc}
only_doc = {k: v for k, v in doc.items() if k not in real}

if only_real:
    print(f"⚠  {len(only_real)} 个端点未记录在 api-routes.md：")
    for k in sorted(only_real):
        print(f"  + {only_real[k]}")
if only_doc:
    print(f"⚠  {len(only_doc)} 个端点已从 Controller 删除：")
    for k in sorted(only_doc):
        print(f"  - {only_doc[k]}")
if not only_real and not only_doc:
    print(f"✓ 文档同步 — {len(real)} 个端点全部记录")

sys.exit(1 if only_real or only_doc else 0)
