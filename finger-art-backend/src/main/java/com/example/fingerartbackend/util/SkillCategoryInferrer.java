package com.example.fingerartbackend.util;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 技能分类推断工具。
 * <p>
 * 从技能标题/描述推断正确的手作分类，修正发布时选错分类导致的展示错误；
 * 分类名称与前端 SKILL_CATEGORIES 保持一致。
 * </p>
 */
public final class SkillCategoryInferrer {

    private static final Map<Pattern, String> RULES = new LinkedHashMap<>();

    static {
        put("缠花|发簪|绒花", "缠花");
        put("拼豆|融合豆|perler", "拼豆");
        put("钩织|毛线|编织|钩针|crochet", "钩织");
        put("滴胶|干花|resin", "滴胶");
        put("穿戴甲|美甲|nails", "穿戴甲");
        put("粘土|黏土|软陶|陶瓷|陶艺|clay", "粘土");
        put("刺绣|embroidery", "刺绣");
        put("串珠|bead", "串珠");
        put("皮艺|皮雕|leather", "皮艺");
        put("木工|木作|wood", "木工");
        put("香薰|蜡烛|candle", "香薰");
        put("纸艺|衍纸|paper", "纸艺");
        put("团扇|tuanshan", "团扇");
        put("花灯|灯笼|lantern", "花灯");
        put("摄影|photography", "摄影");
        put("设计|design", "设计");
    }

    private SkillCategoryInferrer() {}

    /**
     * 执行 put 相关逻辑。
     */
    private static void put(String regex, String category) {
        RULES.put(Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE), category);
    }

    /**
     * 将原始分类名称标准化为标准技能分类（与前端 SKILL_CATEGORIES 一致）。
     *
     * @param raw 原始分类
     * @return 标准分类名称，空值返回「其它」
     */
    public static String normalize(String raw) {
        if (raw == null || raw.isBlank()) return "其它";
        String trimmed = raw.trim();
        return switch (trimmed) {
            case "陶瓷", "软陶", "陶艺", "陶土", "黏土" -> "粘土";
            case "毛线", "编织", "钩针" -> "钩织";
            case "美甲", "甲片" -> "穿戴甲";
            case "绒花", "发簪" -> "缠花";
            case "像素", "融合豆" -> "拼豆";
            default -> trimmed;
        };
    }

    /**
     * 对文本内容应用规则匹配，返回首个命中的分类。
     */
    private static String matchText(String text) {
        if (text == null || text.isBlank()) return null;
        for (Map.Entry<Pattern, String> entry : RULES.entrySet()) {
            if (entry.getKey().matcher(text).find()) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * 综合当前分类、标题与描述推断最终分类。
     * 优先从标题匹配，其次从标题+描述匹配，最后回退到当前分类。
     *
     * @param category    用户选择的分类
     * @param title       技能标题
     * @param description 技能描述
     * @return 推断后的标准分类
     */
    public static String infer(String category, String title, String description) {
        String fromTitle = matchText(title);
        if (fromTitle != null) return normalize(fromTitle);
        String fromAll = matchText(((title != null ? title : "") + " " + (description != null ? description : "")).trim());
        if (fromAll != null) return normalize(fromAll);
        return normalize(category);
    }

    /**
     * 推断结果是否与当前分类不一致（且当前分类可被标题覆盖）。
     *
     * @param category    当前分类
     * @param title       技能标题
     * @param description 技能描述
     * @return 是否需要修正分类
     */
    public static boolean shouldFix(String category, String title, String description) {
        String inferred = infer(category, title, description);
        String normalized = normalize(category);
        return !inferred.equals(normalized);
    }
}
