#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""为 finger-art-backend Java 源码批量补充中文 Javadoc 注释。"""

from __future__ import annotations

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / "src" / "main" / "java" / "com" / "example" / "fingerartbackend"

PACKAGE_DESC = {
    "controller": "REST 控制器",
    "service": "业务服务接口",
    "impl": "业务服务实现",
    "entity": "持久化实体",
    "dto": "数据传输对象",
    "mapper": "数据访问层",
    "auth": "认证鉴权",
    "config": "Spring 配置",
    "util": "工具类",
    "constant": "常量定义",
    "common": "通用组件",
    "realtime": "实时推送",
}

MODULE_CN = {
    "Product": "作品/商品",
    "Order": "订单",
    "User": "用户",
    "Skill": "技能",
    "SkillExchange": "技能交换",
    "CustomRequest": "定制需求",
    "CustomRequestBid": "定制需求揭榜",
    "Review": "评价",
    "Forum": "论坛",
    "Message": "私信",
    "Notification": "通知",
    "Wallet": "钱包",
    "CoinEconomy": "造物币经济",
    "Report": "举报",
    "Analytics": "数据分析",
    "Admin": "管理端",
    "AI": "AI",
    "AiChat": "AI 对话",
    "AiImage": "AI 图像",
    "InspirationGacha": "灵感扭蛋",
    "DemandMatch": "需求匹配",
    "CraftTechnique": "工艺技法",
    "Logistics": "物流",
    "Search": "搜索",
    "ScheduleSlot": "排期",
    "Like": "点赞",
    "SensitiveWord": "敏感词",
    "Totp": "双因素认证",
    "Escrow": "托管交易",
    "PlatformNotification": "平台通知",
    "ContentReport": "内容举报",
    "FingerArtBackendApplication": "应用启动入口",
    "Result": "统一 API 响应",
    "Auth": "认证",
    "Jwt": "JWT",
    "Login": "登录",
    "Realtime": "实时",
    "WebSocket": "WebSocket",
    "Cors": "跨域",
    "Password": "密码",
    "Nickname": "昵称",
    "Category": "分类",
}

SUFFIX_RULES = [
    ("Controller", "控制器，对外提供 HTTP 接口"),
    ("ServiceImpl", "服务实现，处理具体业务逻辑"),
    ("Service", "服务接口，定义业务能力"),
    ("Repository", "仓储接口，封装数据持久化"),
    ("Mapper", "Mapper，负责数据库读写"),
    ("Properties", "配置属性类"),
    ("Config", "配置类"),
    ("Filter", "请求过滤器"),
    ("Interceptor", "请求拦截器"),
    ("Handler", "处理器"),
    ("Factory", "工厂类"),
    ("Runner", "启动时执行的任务"),
    ("Request", "请求体"),
    ("Response", "响应体"),
    ("Result", "结果封装"),
    ("View", "视图/DTO"),
    ("Utils", "工具方法集合"),
    ("Generator", "生成器"),
    ("Inferrer", "推断器"),
    ("Normalizer", "规范化工具"),
]


def module_name(class_name: str) -> str:
    for key, cn in MODULE_CN.items():
        if key in class_name:
            return cn
    base = re.sub(r"(Controller|ServiceImpl|Service|Mapper|Repository|Properties|Config|Filter|Handler|Factory|Runner|Request|Response|Result|View|Utils|Generator|Inferrer|Normalizer)$", "", class_name)
    return base or class_name


def class_comment(class_name: str, pkg: str) -> str:
    pkg_key = pkg.split(".")[-1] if pkg else ""
    pkg_cn = PACKAGE_DESC.get(pkg_key, "")
    mod = module_name(class_name)

    for suffix, desc in SUFFIX_RULES:
        if class_name.endswith(suffix):
            if pkg_cn:
                return f"{mod}{desc}（{pkg_cn}）。"
            return f"{mod}{desc}。"

    if class_name == "FingerArtBackendApplication":
        return "Spring Boot 应用主启动类，负责启动后端服务并注册全局 CORS 配置。"
    if class_name == "Result":
        return "统一 API 响应包装类，包含 code、message、data 字段。"
    if class_name == "AuthUser":
        return "当前登录用户上下文快照，由 JWT 解析得到。"
    if class_name == "AuthContext":
        return "基于 ThreadLocal 的认证上下文，供拦截器与服务层获取当前用户。"
    if pkg_key == "entity":
        return f"{mod}实体，对应数据库表及 JPA 映射。"
    if pkg_key == "dto":
        return f"{mod}数据传输对象，用于接口入参或出参。"
    if pkg_key == "constant":
        return f"{mod}相关常量枚举。"
    return f"{mod}相关类（{pkg_cn or '后端'}）。"


def field_comment(field_name: str, field_type: str) -> str:
    mapping = {
        "id": "主键 ID",
        "userId": "用户 ID",
        "buyerId": "买家 ID",
        "artisanId": "手作人/达人 ID",
        "creatorId": "创作者 ID",
        "productId": "作品 ID",
        "orderId": "订单 ID",
        "requestId": "定制需求 ID",
        "bidId": "揭榜记录 ID",
        "title": "标题",
        "description": "描述",
        "status": "状态",
        "role": "用户角色",
        "username": "用户名",
        "password": "密码（加密存储）",
        "avatar": "头像 URL",
        "image": "图片 URL",
        "price": "价格",
        "stock": "库存",
        "likes": "点赞数",
        "rating": "评分",
        "credit": "信用分",
        "category": "分类",
        "createTime": "创建时间",
        "updateTime": "更新时间",
        "deadline": "截止日期",
        "budgetMin": "预算下限",
        "budgetMax": "预算上限",
        "message": "消息内容",
        "token": "访问令牌",
        "code": "状态码",
        "data": "业务数据载荷",
    }
    if field_name in mapping:
        return mapping[field_name]
    if field_name.endswith("Id"):
        return f"{field_name[:-2]} 关联 ID"
    if field_name.endswith("Time"):
        return f"{field_name[:-4]} 时间"
    if field_name.endswith("Count"):
        return f"{field_name[:-5]} 数量"
    if field_name.endswith("List"):
        return f"{field_name[:-4]} 列表"
    return f"{field_name} 字段"


def method_comment(method_name: str, params: str, class_name: str) -> str:
    mod = module_name(class_name)
    p = params.strip()

    patterns = [
        (r"^get", f"查询{mod}信息"),
        (r"^list", f"查询{mod}列表"),
        (r"^find", f"查找{mod}"),
        (r"^search", f"搜索{mod}"),
        (r"^create", f"创建{mod}"),
        (r"^save", f"保存{mod}"),
        (r"^add", f"新增{mod}"),
        (r"^update", f"更新{mod}"),
        (r"^delete", f"删除{mod}"),
        (r"^remove", f"移除{mod}"),
        (r"^audit", f"审核{mod}"),
        (r"^approve", f"通过{mod}审核"),
        (r"^reject", f"拒绝{mod}"),
        (r"^login", "用户登录"),
        (r"^register", "用户注册"),
        (r"^parse", "解析令牌或数据"),
        (r"^generate", "生成令牌或数据"),
        (r"^build", "构建响应对象"),
        (r"^toggle", f"切换{mod}状态"),
        (r"^submit", f"提交{mod}"),
        (r"^select", f"选择{mod}"),
        (r"^complete", f"完成{mod}"),
        (r"^cancel", f"取消{mod}"),
        (r"^ship", "订单发货"),
        (r"^confirm", "确认操作"),
        (r"^notify", "发送通知"),
        (r"^validate", "校验数据"),
        (r"^assert", "断言业务条件，不满足则抛异常"),
        (r"^is", "判断条件是否成立"),
        (r"^has", "判断是否包含/拥有"),
        (r"^handle", "处理请求或事件"),
        (r"^preHandle", "请求前置拦截处理"),
        (r"^doFilter", "过滤器链处理"),
        (r"^main", "应用入口 main 方法"),
    ]
    for pat, desc in patterns:
        if re.match(pat, method_name):
            if p:
                return f"{desc}。\n     * @param 参数见方法签名"
            return f"{desc}。"

    # fallback
    if p:
        return f"执行 {method_name} 业务逻辑。\n     * @param 参数见方法签名"
    return f"执行 {method_name} 业务逻辑。"


def has_javadoc_before(text: str, pos: int) -> bool:
    before = text[:pos].rstrip()
    return before.endswith("*/")


def insert_class_javadoc(content: str, class_name: str, pkg: str) -> str:
    pattern = re.compile(
        r"(?P<indent>^)(?P<annotations>(?:@\w+(?:\([^)]*\))?\s*)*)"
        r"(?P<decl>(?:public\s+|protected\s+|private\s+)?(?:abstract\s+|final\s+)*"
        r"(?:class|interface|enum|record)\s+" + re.escape(class_name) + r"\b)",
        re.MULTILINE,
    )
    m = pattern.search(content)
    if not m or has_javadoc_before(content, m.start()):
        return content

    comment = class_comment(class_name, pkg)
    block = (
        f"/**\n * {comment}\n */\n"
        f"{m.group('annotations')}{m.group('decl')}"
    )
    return content[: m.start()] + block + content[m.end() :]


def insert_field_javadocs(content: str, is_entity_or_dto: bool) -> str:
    if not is_entity_or_dto:
        return content

    lines = content.split("\n")
    out: list[str] = []
    i = 0
    while i < len(lines):
        line = lines[i]
        m = re.match(r"^(\s*)(private|protected)\s+([\w.<>,\s\[\]]+?)\s+(\w+)\s*;", line)
        if m:
            indent, _, ftype, fname = m.groups()
            prev = out[-1].strip() if out else ""
            prev2 = out[-2].strip() if len(out) > 1 else ""
            if not prev.endswith("*/") and not prev.startswith("@") and not prev2.endswith("*/"):
                fc = field_comment(fname, ftype.strip())
                out.append(f"{indent}/** {fc} */")
        out.append(line)
        i += 1
    return "\n".join(out)


def insert_method_javadocs(content: str, class_name: str) -> str:
    pattern = re.compile(
        r"(?P<indent>^[ \t]*)(?P<annotations>(?:@\w+(?:\([^)]*\))?\s*)*)"
        r"(?P<decl>(?:public|protected|private)\s+(?:static\s+)?[\w.<>,\s\[\]]+\s+(?P<method>\w+)\s*"
        r"\((?P<params>[^)]*)\)\s*(?:throws\s+[\w.\s,]+)?\s*\{)",
        re.MULTILINE,
    )

    matches = list(pattern.finditer(content))
    for m in reversed(matches):
        pos = m.start()
        if has_javadoc_before(content, pos):
            continue
        method_name = m.group("method")
        params = m.group("params") or ""
        if method_name == class_name:
            mc = f"构造 {class_name} 实例。"
        else:
            mc = method_comment(method_name, params, class_name)
        indent = m.group("indent")
        block = f"{indent}/**\n"
        for part in mc.split("\n"):
            block += f"{indent} * {part.strip()}\n"
        block += f"{indent} */\n"
        content = content[:pos] + block + content[pos:]
    return content


def process_file(path: Path) -> bool:
    original = path.read_text(encoding="utf-8")
    pkg_m = re.search(r"package\s+([\w.]+)\s*;", original)
    pkg = pkg_m.group(1) if pkg_m else ""
    pkg_tail = pkg.split(".")[-1]

    class_m = re.search(r"(?:class|interface|enum|record)\s+(\w+)", original)
    if not class_m:
        return False
    class_name = class_m.group(1)

    updated = original
    updated = insert_class_javadoc(updated, class_name, pkg_tail)
    updated = insert_field_javadocs(updated, pkg_tail in ("entity", "dto"))
    updated = insert_method_javadocs(updated, class_name)

    if updated != original:
        path.write_text(updated, encoding="utf-8", newline="\n")
        return True
    return False


def main() -> None:
    files = sorted(ROOT.rglob("*.java"))
    changed = 0
    for f in files:
        if process_file(f):
            changed += 1
            print(f"OK {f.relative_to(ROOT.parent.parent.parent.parent)}")
    print(f"\nDone: {changed}/{len(files)} files updated")


if __name__ == "__main__":
    main()
