package com.example.fingerartbackend.util;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 作品分类推断工具。
 * <p>
 * 基于关键词规则匹配推断作品 category key，与前端 productCategories 保持一致；
 * 用于 AI 发布、批量导入等场景自动补全分类。
 * </p>
 */
public final class ProductCategoryInferrer {

    /** 有效的作品分类 key 集合 */
    public static final Set<String> VALID_KEYS = Set.of(
            "crochet", "resin", "nails", "clay", "flower", "perler", "embroidery",
            "bead", "leather", "wood", "candle", "paper", "tuanshan", "lantern"
    );

    private static final Map<String, String> SHORT_LABEL_TO_KEY = Map.ofEntries(
            Map.entry("钩织", "crochet"),
            Map.entry("滴胶", "resin"),
            Map.entry("穿戴甲", "nails"),
            Map.entry("粘土", "clay"),
            Map.entry("缠花", "flower"),
            Map.entry("拼豆", "perler"),
            Map.entry("刺绣", "embroidery"),
            Map.entry("串珠", "bead"),
            Map.entry("皮艺", "leather"),
            Map.entry("木工", "wood"),
            Map.entry("香薰", "candle"),
            Map.entry("纸艺", "paper"),
            Map.entry("团扇", "tuanshan"),
            Map.entry("花灯", "lantern")
    );

    private static final Map<Pattern, String> CONTEXT_RULES = new LinkedHashMap<>();

    static {
        putContext("团扇|tuanshan|绘扇|扇子", "tuanshan");
        putContext("花灯|灯笼|走马灯|宫灯|lantern", "lantern");
        putContext("缠花|发簪|绒花|头饰|发饰|步摇", "flower");
        putContext("钩织|钩针|毛线|编织|crochet", "crochet");
        putContext("滴胶|干花|树脂|uv胶|resin", "resin");
        putContext("穿戴甲|美甲|甲片|nails", "nails");
        putContext("粘土|黏土|软陶|超轻粘土|clay", "clay");
        putContext("拼豆|融合豆|perler|像素豆", "perler");
        putContext("刺绣|十字绣|戳戳绣|embroidery", "embroidery");
        putContext("串珠|手链|项链|bead", "bead");
        putContext("皮艺|皮雕|皮革|leather", "leather");
        putContext("木工|木作|木雕|wood", "wood");
        putContext("香薰|蜡烛|candle|扩香", "candle");
        putContext("纸艺|衍纸|剪纸|纸雕|paper", "paper");
        putContext("摆件|玩偶|公仔|手办", "clay");
    }

    private ProductCategoryInferrer() {}

    /**
     * 执行 putContext 相关逻辑。
     */
    private static void putContext(String regex, String key) {
        CONTEXT_RULES.put(Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE), key);
    }

    /**
     * 判断 category key 是否在有效集合内。
     *
     * @param key 分类 key
     * @return 是否有效
     */
    public static boolean isValidKey(String key) {
        return key != null && VALID_KEYS.contains(key);
    }

    /**
     * 从关键词推断 category key；无把握时返回 {@code null}。
     *
     * @param keywords 作品关键词或描述片段
     * @return 推断的 category key，无法推断返回 null
     */
    public static String inferKey(String keywords) {
        if (keywords == null || keywords.isBlank()) return null;

        String trimmed = keywords.trim();
        String normalized = CategoryNormalizer.normalize(trimmed);
        if (isValidKey(normalized)) return normalized;

        for (Map.Entry<Pattern, String> entry : CONTEXT_RULES.entrySet()) {
            if (entry.getKey().matcher(trimmed).find()) {
                return entry.getValue();
            }
        }

        String shortLabel = SkillCategoryInferrer.infer(null, trimmed, null);
        if (!"其它".equals(shortLabel) && SHORT_LABEL_TO_KEY.containsKey(shortLabel)) {
            return SHORT_LABEL_TO_KEY.get(shortLabel);
        }

        String lower = trimmed.toLowerCase(Locale.ROOT);
        if (containsAny(lower, "新中式", "国风", "古风", "汉服", "中式", "传统")) {
            if (containsAny(lower, "扇")) return "tuanshan";
            if (containsAny(lower, "灯", "笼")) return "lantern";
            if (containsAny(lower, "簪", "头饰", "发饰")) return "flower";
            return "flower";
        }

        return null;
    }

    /**
     * 判断规则是否命中明确工艺词（非泛化兜底）。
     *
     * @param keywords 关键词
     * @param key      待验证的分类 key
     * @return 是否高置信度匹配
     */
    public static boolean isConfidentMatch(String keywords, String key) {
        if (!isValidKey(key) || keywords == null || keywords.isBlank()) return false;
        String inferred = inferKey(keywords);
        return key.equals(inferred);
    }

    /**
     * 清洗并规范化原始 category 字符串为有效 key。
     *
     * @param raw 原始分类值
     * @return 有效 key，无法清洗返回 null
     */
    public static String sanitizeKey(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String trimmed = raw.trim().toLowerCase(Locale.ROOT);
        if (isValidKey(trimmed)) return trimmed;

        for (String key : VALID_KEYS) {
            if (trimmed.contains(key)) return key;
        }

        String normalized = CategoryNormalizer.normalize(trimmed);
        return isValidKey(normalized) ? normalized : null;
    }

    /**
     * 执行 containsAny 相关逻辑。
     */
    private static boolean containsAny(String text, String... keywords) {
        for (String kw : keywords) {
            if (text.contains(kw)) return true;
        }
        return false;
    }
}
