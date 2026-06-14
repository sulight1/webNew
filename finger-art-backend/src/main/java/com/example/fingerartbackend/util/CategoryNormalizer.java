package com.example.fingerartbackend.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class CategoryNormalizer {
    private static final Map<String, String> ALIASES = new HashMap<>();

    static {
        put("钩织", "crochet");
        put("钩织系列", "crochet");
        put("crochet", "crochet");
        put("滴胶", "resin");
        put("滴胶干花", "resin");
        put("resin", "resin");
        put("穿戴甲", "nails");
        put("精致穿戴甲", "nails");
        put("nails", "nails");
        put("粘土", "clay");
        put("软陶粘土", "clay");
        put("clay", "clay");
        put("缠花", "flower");
        put("古法缠花", "flower");
        put("flower", "flower");
        put("拼豆", "perler");
        put("拼豆系列", "perler");
        put("perler", "perler");
        put("刺绣", "embroidery");
        put("刺绣布艺", "embroidery");
        put("embroidery", "embroidery");
        put("串珠", "bead");
        put("串珠饰品", "bead");
        put("bead", "bead");
        put("皮艺", "leather");
        put("皮艺皮雕", "leather");
        put("leather", "leather");
        put("木工", "wood");
        put("木工雕绘", "wood");
        put("wood", "wood");
        put("香薰", "candle");
        put("香薰蜡烛", "candle");
        put("candle", "candle");
        put("纸艺", "paper");
        put("纸艺衍纸", "paper");
        put("paper", "paper");
        put("摄影", "photography");
        put("photography", "photography");
        put("设计", "design");
        put("design", "design");
        put("其它", "other");
        put("other", "other");
    }

    private CategoryNormalizer() {}

    private static void put(String key, String normalized) {
        ALIASES.put(key.toLowerCase(Locale.ROOT), normalized);
    }

    public static String normalize(String raw) {
        if (raw == null || raw.isBlank()) return "other";
        String trimmed = raw.trim();
        String direct = ALIASES.get(trimmed.toLowerCase(Locale.ROOT));
        if (direct != null) return direct;
        for (Map.Entry<String, String> entry : ALIASES.entrySet()) {
            if (trimmed.toLowerCase(Locale.ROOT).contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return trimmed.toLowerCase(Locale.ROOT);
    }

    public static boolean matches(String a, String b) {
        return normalize(a).equals(normalize(b));
    }

    public static Set<String> keys() {
        return ALIASES.keySet();
    }
}
