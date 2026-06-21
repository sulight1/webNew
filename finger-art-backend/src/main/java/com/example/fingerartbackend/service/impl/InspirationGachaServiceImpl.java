package com.example.fingerartbackend.service.impl;

import com.example.fingerartbackend.entity.CoinTaskClaim;
import com.example.fingerartbackend.entity.User;
import com.example.fingerartbackend.mapper.CoinTaskClaimMapper;
import com.example.fingerartbackend.mapper.UserMapper;
import com.example.fingerartbackend.service.AiImageService;
import com.example.fingerartbackend.service.InspirationGachaService;
import com.example.fingerartbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
public class InspirationGachaServiceImpl implements InspirationGachaService {

    public static final String TASK_DAILY_FREE = "daily_gacha_free";
    public static final int EXTRA_DRAW_COST = 10;
    public static final int IMAGE_GEN_COST = 15;

    private static final List<String> STYLE_KEYS = List.of(
            "newChinese", "y2k", "dopamine", "retro", "cute", "luxury"
    );

    private static final Map<String, String> STYLE_LABELS = Map.of(
            "newChinese", "新中式",
            "y2k", "Y2K辣妹",
            "dopamine", "多巴胺",
            "retro", "复古文艺",
            "cute", "软萌可爱",
            "luxury", "轻奢精致"
    );

    private static final List<CraftOption> CRAFTS = List.of(
            new CraftOption("crochet", "钩织系列", "🧶"),
            new CraftOption("resin", "滴胶干花", "💎"),
            new CraftOption("nails", "精致穿戴甲", "💅"),
            new CraftOption("clay", "软陶粘土", "🏺"),
            new CraftOption("flower", "古法缠花", "🌸"),
            new CraftOption("perler", "拼豆系列", "🫘"),
            new CraftOption("embroidery", "刺绣布艺", "🪡"),
            new CraftOption("bead", "串珠饰品", "📿"),
            new CraftOption("candle", "香薰蜡烛", "🕯️"),
            new CraftOption("paper", "纸艺衍纸", "📄")
    );

    private static final List<ColorPalette> PALETTES = List.of(
            new ColorPalette("樱花物语", "#FFB7C5", "#FFF5F7", "#FFD6E7"),
            new ColorPalette("薄荷汽水", "#7FDBCA", "#E8FFF9", "#B8F2E6"),
            new ColorPalette("落日橘光", "#FF9F68", "#FFE8D6", "#FF6B6B"),
            new ColorPalette("雾紫梦境", "#B794F6", "#F3E8FF", "#9F7AEA"),
            new ColorPalette("海盐牛乳", "#A8DADC", "#F1FAEE", "#457B9D"),
            new ColorPalette("黑糖拿铁", "#8D6E63", "#EFEBE9", "#D7CCC8"),
            new ColorPalette("柠檬多巴胺", "#FFE066", "#FF6B9D", "#4ECDC4"),
            new ColorPalette("墨染东方", "#2D3748", "#E2E8F0", "#C53030")
    );

    private static final Map<String, List<String>> COPY_TEMPLATES = Map.of(
            "newChinese", List.of(
                    "把东方留白写进{craft}，{palette}像一幅会呼吸的小画。",
                    "一缕{craft}的雅致，{palette}配色的{style}，适合日常点睛。",
                    "用{craft}留住风雅，{palette}是今天最温柔的主色。"
            ),
            "y2k", List.of(
                    "千禧辣妹上线！{craft} + {palette}，出片率拉满。",
                    "把{craft}做成Y2K小单品，{palette}撞色很上头。",
                    "甜酷{style}的{craft}，{palette}是你的今日人设色。"
            ),
            "dopamine", List.of(
                    "快乐配色暴击！{craft}用{palette}，心情自动+100。",
                    "把彩虹装进{craft}，{palette}是今日能量色。",
                    "元气{craft}灵感：{palette}，看见就想动手做。"
            ),
            "retro", List.of(
                    "旧时光滤镜下的{craft}，{palette}自带故事感。",
                    "法式慵懒 × {craft}，{palette}温柔又不无聊。",
                    "Vintage 味儿的{craft}，{palette}像从相册里走出来。"
            ),
            "cute", List.of(
                    "软fufu的{craft}！{palette}配色萌到心化。",
                    "治愈系{craft}小物，{palette}是今日可爱密码。",
                    "把{craft}做成礼物吧，{palette}谁都会喜欢。"
            ),
            "luxury", List.of(
                    "小众又高级：{craft} × {palette}，质感在线。",
                    "轻奢{craft}灵感，{palette}低调但有记忆点。",
                    "用{craft}做一件「看起来很贵」的小物，{palette}刚好。"
            )
    );

    @Autowired
    private CoinTaskClaimMapper claimMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private AiImageService aiImageService;

    private final Random random = new Random();

    @Override
    public Map<String, Object> getStatus(Long userId) {
        User user = userMapper.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));
        boolean freeAvailable = !claimMapper.existsByUserIdAndTaskCodeAndClaimDate(
                userId, TASK_DAILY_FREE, LocalDate.now());
        Map<String, Object> status = new HashMap<>();
        status.put("freeAvailable", freeAvailable);
        status.put("extraDrawCost", EXTRA_DRAW_COST);
        status.put("imageGenCost", IMAGE_GEN_COST);
        status.put("balance", user.getZaowuBiBalance() != null ? user.getZaowuBiBalance() : 0);
        return status;
    }

    @Override
    @Transactional
    public Map<String, Object> draw(Long userId, boolean useFree) {
        User user = userMapper.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));
        LocalDate today = LocalDate.now();
        boolean freeAvailable = !claimMapper.existsByUserIdAndTaskCodeAndClaimDate(
                userId, TASK_DAILY_FREE, today);

        String costType;
        if (useFree) {
            if (!freeAvailable) {
                throw new RuntimeException("今日免费次数已用完");
            }
            markFreeUsed(userId, today);
            costType = "free";
        } else {
            double balance = user.getZaowuBiBalance() != null ? user.getZaowuBiBalance() : 0;
            if (balance < EXTRA_DRAW_COST) {
                throw new RuntimeException("造物币不足，再次扭蛋需要 " + EXTRA_DRAW_COST + " 币");
            }
            user = userService.addZaoWuBi(userId, (double) -EXTRA_DRAW_COST);
            costType = "paid";
        }

        Map<String, Object> result = buildRandomInspiration();
        result.put("costType", costType);
        result.put("freeAvailable", !claimMapper.existsByUserIdAndTaskCodeAndClaimDate(
                userId, TASK_DAILY_FREE, today));
        result.put("extraDrawCost", EXTRA_DRAW_COST);
        result.put("imageGenCost", IMAGE_GEN_COST);
        result.put("balance", user.getZaowuBiBalance() != null ? user.getZaowuBiBalance() : 0);
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> generateImage(Long userId, String imagePrompt) {
        if (imagePrompt == null || imagePrompt.isBlank()) {
            throw new RuntimeException("缺少生图描述");
        }
        User user = userMapper.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));
        double balance = user.getZaowuBiBalance() != null ? user.getZaowuBiBalance() : 0;
        if (balance < IMAGE_GEN_COST) {
            throw new RuntimeException("造物币不足，生成参考图需要 " + IMAGE_GEN_COST + " 币");
        }
        user = userService.addZaoWuBi(userId, (double) -IMAGE_GEN_COST);
        try {
            String imageUrl = aiImageService.generateAndSave(imagePrompt.trim());
            Map<String, Object> result = new HashMap<>();
            result.put("imageUrl", imageUrl);
            result.put("balance", user.getZaowuBiBalance());
            result.put("imageGenCost", IMAGE_GEN_COST);
            return result;
        } catch (Exception e) {
            userService.addZaoWuBi(userId, (double) IMAGE_GEN_COST);
            throw new RuntimeException("参考图生成失败：" + (e.getMessage() != null ? e.getMessage() : "请稍后重试"));
        }
    }

    private void markFreeUsed(Long userId, LocalDate today) {
        CoinTaskClaim claim = new CoinTaskClaim();
        claim.setUserId(userId);
        claim.setTaskCode(TASK_DAILY_FREE);
        claim.setClaimDate(today);
        claim.setCoinsGranted(0);
        claimMapper.save(claim);
    }

    private Map<String, Object> buildRandomInspiration() {
        String styleKey = STYLE_KEYS.get(random.nextInt(STYLE_KEYS.size()));
        CraftOption craft = CRAFTS.get(random.nextInt(CRAFTS.size()));
        ColorPalette palette = PALETTES.get(random.nextInt(PALETTES.size()));

        String styleLabel = STYLE_LABELS.get(styleKey);
        String copy = buildCopy(styleKey, craft.label(), styleLabel, palette.name());
        String title = buildTitle(craft, styleLabel, palette.name());
        String description = buildDescription(craft, styleLabel, palette.name(), copy);
        String imagePrompt = buildImagePrompt(craft, styleLabel, palette);
        int suggestedPrice = suggestPrice(craft.key(), styleKey);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("style", styleKey);
        result.put("styleLabel", styleLabel);
        result.put("category", craft.key());
        result.put("categoryLabel", craft.label());
        result.put("categoryEmoji", craft.emoji());
        result.put("paletteName", palette.name());
        result.put("colors", palette.toColorList());
        result.put("colorHint", palette.colorHint());
        result.put("copy", copy);
        result.put("title", title);
        result.put("description", description);
        result.put("imagePrompt", imagePrompt);
        result.put("suggestedPrice", suggestedPrice);
        result.put("tags", buildTags(craft, styleLabel));
        return result;
    }

    private String buildCopy(String styleKey, String craftLabel, String styleLabel, String paletteName) {
        List<String> templates = COPY_TEMPLATES.getOrDefault(styleKey, COPY_TEMPLATES.get("cute"));
        String template = templates.get(random.nextInt(templates.size()));
        return template
                .replace("{craft}", craftLabel)
                .replace("{style}", styleLabel)
                .replace("{palette}", paletteName);
    }

    private String buildTitle(CraftOption craft, String styleLabel, String paletteName) {
        List<String> titles = List.of(
                "【灵感盲盒】" + craft.emoji() + craft.label() + " · " + styleLabel,
                paletteName + "系" + craft.label() + " · " + styleLabel + "风",
                "今日手作灵感 · " + craft.label() + " × " + styleLabel
        );
        return titles.get(random.nextInt(titles.size()));
    }

    private String buildDescription(CraftOption craft, String styleLabel, String paletteName, String copy) {
        return copy + " 推荐工艺：" + craft.label() + "；风格：" + styleLabel + "；主配色：" + paletteName
                + "。可在指尖造物发布作品或定制需求，让灵感变成真实手作。";
    }

    private String buildImagePrompt(CraftOption craft, String styleLabel, ColorPalette palette) {
        return "精美手工" + craft.label() + "作品，" + styleLabel + "风格，"
                + palette.colorHint() + "配色，高清产品摄影，柔和自然光，白色背景，细节丰富，8k";
    }

    private int suggestPrice(String categoryKey, String styleKey) {
        int base = switch (categoryKey) {
            case "nails" -> 68;
            case "resin", "bead" -> 58;
            case "crochet", "embroidery" -> 88;
            case "clay", "flower" -> 78;
            default -> 65;
        };
        if ("luxury".equals(styleKey)) base += 30;
        if ("cute".equals(styleKey) || "dopamine".equals(styleKey)) base += 10;
        return base + random.nextInt(20);
    }

    private List<String> buildTags(CraftOption craft, String styleLabel) {
        return List.of("灵感盲盒", craft.label(), styleLabel, "指尖造物");
    }

    private record CraftOption(String key, String label, String emoji) {
    }

    private record ColorPalette(String name, String primary, String secondary, String accent) {
        String colorHint() {
            return name + "（" + primary + " / " + secondary + " / " + accent + "）";
        }

        List<Map<String, String>> toColorList() {
            return List.of(
                    colorMap("主色", primary),
                    colorMap("辅色", secondary),
                    colorMap("点缀", accent)
            );
        }

        private Map<String, String> colorMap(String label, String hex) {
            Map<String, String> map = new LinkedHashMap<>();
            map.put("label", label);
            map.put("hex", hex);
            return map;
        }
    }
}
