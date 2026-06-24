#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""清理批量注释脚本产生的格式问题与低质量占位注释。"""

from __future__ import annotations

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / "src" / "main" / "java"

LIFECYCLE_COMMENTS = {
    "onCreate": "JPA 持久化前回调，自动写入创建时间。",
    "onUpdate": "JPA 更新前回调，自动刷新更新时间。",
    "onPrePersist": "实体首次入库前的预处理。",
    "onPreUpdate": "实体更新入库前的预处理。",
}


def cleanup_content(text: str) -> str:
    lines = text.split("\n")
    out: list[str] = []
    i = 0
    while i < len(lines):
        line = lines[i]
        stripped = line.strip()

        # 删除错误的 * * @param 行
        if stripped in ("* * @param 参数见方法签名", "* @param 参数见方法签名"):
            i += 1
            continue

        # 修正冗余的类注释
        if stripped.endswith("，处理具体业务逻辑（业务服务实现）。"):
            line = line.replace("，处理具体业务逻辑（业务服务实现）。", "实现类。")

        # 修正错误的 CORS 方法注释块（整块替换）
        if stripped == "* 新增应用启动入口。":
            # 跳过直到 */
            while i < len(lines) and "*/" not in lines[i]:
                i += 1
            i += 1
            out.append("            /** 配置允许跨域访问的路径、来源与方法。 */")
            continue

        out.append(line)

        # 修正 onCreate/onUpdate 等生命周期方法注释
        if stripped == "*/" and i + 1 < len(lines):
            nxt = lines[i + 1].strip()
            for name, comment in LIFECYCLE_COMMENTS.items():
                if nxt.startswith(f"public void {name}(") or nxt.startswith(f"protected void {name}("):
                    # 回看 out 中上一个 /** 块并替换
                    j = len(out) - 1
                    while j >= 0 and not out[j].strip().startswith("/**"):
                        j -= 1
                    if j >= 0 and f"执行 {name} 业务逻辑" in "\n".join(out[j:]):
                        block_end = len(out)
                        out = out[:j]
                        indent = re.match(r"^(\s*)", lines[i + 1]).group(1)
                        out.append(f"{indent}/** {comment} */")
                    break
        i += 1

    text = "\n".join(out)

    # 类注释简化
    text = re.sub(
        r"(\* [^*\n]+)实现，处理具体业务逻辑（业务服务实现）。",
        r"\1实现类。",
        text,
    )
    text = re.sub(
        r"(\* [^*\n]+)服务实现，处理具体业务逻辑（业务服务实现）。",
        r"\1服务实现类。",
        text,
    )

    # 生命周期方法：执行 onXxx 业务逻辑 -> 具体说明
    for name, comment in LIFECYCLE_COMMENTS.items():
        text = text.replace(f"* 执行 {name} 业务逻辑。", f"* {comment}")

    # 删除仅含「执行 xxx 业务逻辑」且下一行无意义的重复块（保留首行描述类方法的可留）
    text = re.sub(
        r"/\*\*\n(\s+)\* 执行 (\w+) 业务逻辑。\n\1\*/\n",
        lambda m: f"/**\n{m.group(1)}* 执行 {m.group(2)} 相关逻辑。\n{m.group(1)}*/\n",
        text,
    )

    return text


def main() -> None:
    changed = 0
    for path in sorted(ROOT.rglob("*.java")):
        original = path.read_text(encoding="utf-8")
        updated = cleanup_content(original)
        if updated != original:
            path.write_text(updated, encoding="utf-8", newline="\n")
            changed += 1
    print(f"Cleaned {changed} files")


if __name__ == "__main__":
    main()
