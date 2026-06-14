package com.example.fingerartbackend.controller;

import com.example.fingerartbackend.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/ai")
public class AIController {

    private static final List<String> ALL_STYLES = Arrays.asList("newChinese", "y2k", "dopamine", "retro", "cute", "luxury");

    // ====== 测试接口 ======
    @GetMapping("/test")
    public Result<Map<String, Object>> test() {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "后端工作正常！");
        data.put("imageUrl", "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=1024");
        return Result.success(data);
    }

    // ====== AI 绘画（通义万相，国内） ======
    @Autowired
    private com.example.fingerartbackend.service.AiImageService aiImageService;

    @PostMapping("/generate-drawing")
    public Result<Map<String, Object>> generateDrawing(@RequestBody Map<String, String> payload) {
        String prompt = payload.get("prompt");
        System.out.println("收到AI绘画请求，提示词: " + prompt);

        if (prompt == null || prompt.isEmpty()) {
            return Result.error("描述词不能为空");
        }

        try {
            String imageUrl = aiImageService.generateAndSave(prompt);
            Map<String, Object> data = new HashMap<>();
            data.put("imageUrl", imageUrl);
            data.put("apiUsed", "通义万相");
            System.out.println("生图完成，本地URL: " + imageUrl);
            return Result.success(data);
        } catch (Exception e) {
            System.err.println("AI绘画出错: " + e.getMessage());
            return Result.error(e.getMessage() != null ? e.getMessage() : "生成失败");
        }
    }

    // ====== AI 文案生成 ======
    @PostMapping("/generate-copywriting")
    public Result<Map<String, Object>> generateCopywriting(@RequestBody Map<String, String> payload) {
        String keywords = payload.get("keywords");
        String style = payload.get("style");

        if (keywords == null || keywords.isEmpty()) {
            return Result.error("关键词不能为空");
        }

        String detectedStyle = style;
        if (style == null || style.equals("auto")) {
            detectedStyle = detectStyle(keywords);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("title", generateTitle(keywords, detectedStyle));
        data.put("description", generateDescription(keywords, detectedStyle));
        data.put("tags", generateTags(keywords, detectedStyle));
        data.put("style", detectedStyle);

        return Result.success(data);
    }

    // ====== 定制需求结构化助手 ======
    @PostMapping("/structure-custom-request")
    public Result<Map<String, Object>> structureCustomRequest(@RequestBody Map<String, String> payload) {
        String brief = payload.get("brief");
        if (brief == null || brief.trim().isEmpty()) {
            return Result.error("请先描述你的定制想法");
        }
        String imagePrompt = payload.get("imagePrompt");
        String combined = brief + (imagePrompt != null ? " " + imagePrompt : "");

        String category = detectRequestCategory(combined);
        String material = detectMaterial(combined, category);
        String size = detectSize(combined, category);
        int[] budget = suggestBudget(category, combined);
        int leadDays = suggestLeadDays(category, combined);
        String deadline = java.time.LocalDate.now().plusDays(leadDays).toString();
        String title = buildRequestTitle(brief, category);
        String description = buildStructuredDescription(brief, category, size, material, imagePrompt);

        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("category", category);
        data.put("size", size);
        data.put("material", material);
        data.put("budgetMin", budget[0]);
        data.put("budgetMax", budget[1]);
        data.put("leadDays", leadDays);
        data.put("deadline", deadline);
        data.put("description", description);
        return Result.success(data);
    }

    // ====== 作品上架助手 ======
    @PostMapping("/listing-assistant")
    public Result<Map<String, Object>> listingAssistant(@RequestBody Map<String, String> payload) {
        String keywords = payload.get("keywords");
        if (keywords == null || keywords.trim().isEmpty()) {
            return Result.error("请输入作品关键词或卖点");
        }
        String style = payload.get("style");
        if (style == null || style.isEmpty()) style = "auto";
        String detectedStyle = style.equals("auto") ? detectStyle(keywords) : style;
        String category = detectProductCategory(keywords);
        String craftTechnique = suggestCraftTechnique(category, keywords);
        int suggestedPrice = suggestPrice(category, keywords);

        Map<String, Object> data = new HashMap<>();
        data.put("title", generateTitle(keywords, detectedStyle));
        data.put("description", generateDescription(keywords, detectedStyle));
        data.put("tags", generateTags(keywords, detectedStyle));
        data.put("style", detectedStyle);
        data.put("category", category);
        data.put("craftTechnique", craftTechnique);
        data.put("suggestedPrice", suggestedPrice);
        return Result.success(data);
    }

    // ====== 技能发布助手 ======
    @PostMapping("/skill-assistant")
    public Result<Map<String, Object>> skillAssistant(@RequestBody Map<String, String> payload) {
        String keywords = payload.get("keywords");
        if (keywords == null || keywords.trim().isEmpty()) {
            return Result.error("请先描述你能提供的技能");
        }
        String category = detectSkillCategory(keywords);
        String duration = suggestSkillDuration(category, keywords);
        int zaowuBiCost = suggestSkillCost(category, keywords);
        String title = buildSkillTitle(keywords, category);
        String description = buildSkillDescription(keywords, category, duration);

        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("description", description);
        data.put("category", category);
        data.put("duration", duration);
        data.put("zaowuBiCost", zaowuBiCost);
        data.put("tags", generateTags(keywords, detectStyle(keywords)));
        return Result.success(data);
    }

    @Autowired
    private com.example.fingerartbackend.service.AiChatService aiChatService;

    // ====== AI 造物管家聊天 ======
    @PostMapping("/chat")
    public Result<Map<String, Object>> chat(@RequestBody Map<String, Object> payload) {
        @SuppressWarnings("unchecked")
        List<Map<String, String>> messages = (List<Map<String, String>>) payload.get("messages");

        if (messages == null || messages.isEmpty()) {
            return Result.error("消息不能为空");
        }

        Long userId = null;
        Object userIdObj = payload.get("userId");
        if (userIdObj instanceof Number) {
            userId = ((Number) userIdObj).longValue();
        } else if (userIdObj != null) {
            try {
                userId = Long.parseLong(userIdObj.toString());
            } catch (NumberFormatException ignored) {
            }
        }

        String pageContext = payload.get("pageContext") != null ? payload.get("pageContext").toString() : null;

        try {
            return Result.success(aiChatService.chat(messages, userId, pageContext));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("AI 对话失败：" + e.getMessage());
        }
    }

    // ====== AI 智能推荐 ======
    @PostMapping("/recommend")
    public Result<Map<String, Object>> recommend(@RequestBody Map<String, Object> payload) {
        String query = payload.get("query") != null ? payload.get("query").toString() : "";
        int limit = 5;
        Object limitObj = payload.get("limit");
        if (limitObj instanceof Number) {
            limit = ((Number) limitObj).intValue();
        }
        try {
            return Result.success(aiChatService.recommend(query, limit));
        } catch (Exception e) {
            return Result.error("推荐失败：" + e.getMessage());
        }
    }

    // ====== 风格检测 ======
    private static String detectStyle(String keywords) {
        String lowerKw = keywords.toLowerCase();

        if (containsAny(lowerKw, "汉服", "古风", "中式", "缠花", "团扇", "簪", "民族", "传统")) return "newChinese";
        if (containsAny(lowerKw, "y2k", "千禧", "辣妹", "亚", "酷", "街头", "夸张")) return "y2k";
        if (containsAny(lowerKw, "多巴胺", "荧光", "彩色", "亮", "彩虹", "活泼")) return "dopamine";
        if (containsAny(lowerKw, "复古", "vintage", "港风", "文艺", "法式", "怀旧")) return "retro";
        if (containsAny(lowerKw, "可爱", "萌", "软", "粘土", "猫咪", "兔子", "少女", "甜")) return "cute";
        if (containsAny(lowerKw, "轻奢", "精致", "高级", "银饰", "天然石", "品质")) return "luxury";

        return ALL_STYLES.get(new Random().nextInt(ALL_STYLES.size()));
    }

    private static boolean containsAny(String text, String... keywords) {
        for (String kw : keywords) {
            if (text.contains(kw)) return true;
        }
        return false;
    }

    // ====== 标题生成 ======
    private static String generateTitle(String keywords, String style) {
        Map<String, List<String>> templates = new HashMap<>();
        templates.put("newChinese", Arrays.asList(
                "【新中式美学】" + keywords + " · 东方雅致",
                "🏮 指尖造物 · 原创" + keywords,
                "东方韵味 · " + keywords + " · 传承之作",
                "【风雅集】" + keywords + " · 古典新绎"));
        templates.put("y2k", Arrays.asList(
                "✨ Y2K辣妹必备 · " + keywords,
                "【千禧回潮】" + keywords + " · 个性定制",
                "亚文化风 · " + keywords + " · 甜酷出街",
                "复古未来主义 · " + keywords));
        templates.put("dopamine", Arrays.asList(
                "🌈 多巴胺色彩 · " + keywords,
                "【快乐因子】" + keywords + " · 点亮心情",
                "色彩收藏家 · " + keywords,
                "彩虹糖般的" + keywords));
        templates.put("retro", Arrays.asList(
                "📻 复古文艺风 · " + keywords,
                "【旧时光】" + keywords + " · 独家记忆",
                "法式浪漫 · " + keywords,
                "复古回潮 · " + keywords));
        templates.put("cute", Arrays.asList(
                "🌸 软萌可爱 · " + keywords,
                "【治愈系】" + keywords,
                "萌化了！" + keywords,
                "【甜系少女】" + keywords + " · 少女心爆棚"));
        templates.put("luxury", Arrays.asList(
                "💎 轻奢精致 · " + keywords,
                "【高级感】" + keywords + " · 小众设计",
                "匠心之作 · " + keywords,
                "【臻品】" + keywords + " · 气质之选"));

        List<String> styleTemplates = templates.getOrDefault(style, templates.get("newChinese"));
        return styleTemplates.get(new Random().nextInt(styleTemplates.size()));
    }

    // ====== 描述生成 ======
    private static String generateDescription(String keywords, String style) {
        Map<String, List<String>> templates = new HashMap<>();
        templates.put("newChinese", Arrays.asList(
                "这款" + keywords + "完美诠释了东方美学的独特魅力。从选材到成品，每一道工序都由经验丰富的手作达人亲手完成。",
                "独具匠心的" + keywords + "，融合了传统工艺与现代审美。每一个细节都经过精心打磨，展现出东方雅致的韵味。",
                "新中式风格的" + keywords + "，将古典元素与现代设计完美结合。手工制作的温度，机器无法复制。"));
        templates.put("y2k", Arrays.asList(
                "千禧年回潮！这款" + keywords + "简直是Y2K辣妹必备单品！独特的设计，亮眼的色彩，让你在人群中脱颖而出！",
                "又酷又甜的" + keywords + "，完美诠释了千禧年的复古未来主义。手工定制，独一无二！",
                "亚文化风拉满的" + keywords + "，个性十足！纯手工制作，每一个细节都充满巧思。"));
        templates.put("dopamine", Arrays.asList(
                "色彩控必入！这款" + keywords + "采用大胆的多巴胺配色，仿佛把彩虹戴在身上～",
                "点亮心情的" + keywords + "！多巴胺色彩搭配，让你成为行走的快乐源泉～",
                "🌈 彩虹糖般的" + keywords + "！色彩饱满，元气满满！纯手工制作！"));
        templates.put("retro", Arrays.asList(
                "复古文艺风的" + keywords + "，仿佛从旧时光里走来～带着淡淡的怀旧气息，温柔又有质感。",
                "法式浪漫风的" + keywords + "，慵懒又迷人～手工制作的温度，让它更有故事感。",
                "【Vintage】复古回潮！这款" + keywords + "完美复刻了那个年代的优雅与精致。"));
        templates.put("cute", Arrays.asList(
                "🌸 软萌可爱的" + keywords + "！让人一眼就爱上～纯手工制作，每一个细节都透露着可爱气息。",
                "治愈系的" + keywords + "！软萌的设计，仿佛能治愈一切不开心～",
                "萌化了！这款" + keywords + "也太可爱了吧～纯手工制作！"));
        templates.put("luxury", Arrays.asList(
                "💎 轻奢精致的" + keywords + "！高级感满满～纯手工制作，每一个细节都精益求精。",
                "【小众设计】这款" + keywords + "采用优质材料，手工精心制作。不撞款，有格调！",
                "匠心之作的" + keywords + "！低调奢华，质感一流。纯手工制作，每一件都是精品。"));

        List<String> styleTemplates = templates.getOrDefault(style, templates.get("newChinese"));
        return styleTemplates.get(new Random().nextInt(styleTemplates.size()));
    }

    // ====== 标签生成 ======
    private static List<String> generateTags(String keywords, String style) {
        List<String> baseTags = Arrays.asList("指尖造物", "原创手作");

        Map<String, List<String>> styleTags = new HashMap<>();
        styleTags.put("newChinese", Arrays.asList("新中式", "古风", "东方美学", "汉服配饰"));
        styleTags.put("y2k", Arrays.asList("Y2K", "千禧风", "辣妹", "亚文化"));
        styleTags.put("dopamine", Arrays.asList("多巴胺", "彩色", "元气", "夏日必备"));
        styleTags.put("retro", Arrays.asList("复古", "文艺", "Vintage", "法式"));
        styleTags.put("cute", Arrays.asList("可爱", "软萌", "治愈系", "少女心"));
        styleTags.put("luxury", Arrays.asList("轻奢", "精致", "小众设计", "高级感"));

        List<String> keywordTags = Arrays.asList(keywords.split("[,，、 ]"));
        keywordTags = keywordTags.stream().filter(t -> !t.trim().isEmpty()).toList();
        List<String> styleTagList = styleTags.getOrDefault(style, styleTags.get("newChinese"));

        Set<String> allTags = new LinkedHashSet<>();
        allTags.addAll(baseTags);
        allTags.addAll(styleTagList);
        allTags.addAll(keywordTags);

        List<String> result = new ArrayList<>(allTags);
        return result.size() > 8 ? result.subList(0, 8) : result;
    }

    private static String detectRequestCategory(String text) {
        String lower = text.toLowerCase();
        if (containsAny(lower, "钩织", "钩针", "毛线", "编织")) return "钩织";
        if (containsAny(lower, "滴胶", "干花", "树脂")) return "滴胶";
        if (containsAny(lower, "穿戴甲", "美甲", "指甲")) return "穿戴甲";
        if (containsAny(lower, "粘土", "黏土", "软陶")) return "粘土";
        if (containsAny(lower, "缠花", "团扇", "发簪")) return "缠花";
        if (containsAny(lower, "拼豆", "像素")) return "拼豆";
        if (containsAny(lower, "刺绣", "绣")) return "刺绣";
        if (containsAny(lower, "串珠", "手链", "项链")) return "串珠";
        return "其它";
    }

    private static String detectMaterial(String text, String category) {
        String lower = text.toLowerCase();
        if (containsAny(lower, "棉线", "牛奶棉", "毛线")) return "牛奶棉 / 5股棉线";
        if (containsAny(lower, "树脂", "滴胶", "uv")) return "UV树脂 + 干花/色精";
        if (containsAny(lower, "银", "铜", "金属")) return "铜丝 / 银饰配件";
        if (containsAny(lower, "皮", "皮革")) return "植鞣皮 / 头层牛皮";
        if (containsAny(lower, "木", "胡桃", "松木")) return "实木 / 胡桃木";
        return switch (category) {
            case "钩织" -> "牛奶棉线";
            case "滴胶" -> "UV树脂";
            case "穿戴甲" -> "甲片 + 光疗胶";
            case "粘土" -> "超轻粘土 / 软陶";
            case "缠花" -> "蚕丝线 + 铜丝";
            case "拼豆" -> "融合豆";
            case "刺绣" -> "绣线 + 亚麻布";
            case "串珠" -> "玻璃珠 / 天然石";
            default -> "按设计选用合适材料";
        };
    }

    private static String detectSize(String text, String category) {
        String lower = text.toLowerCase();
        if (containsAny(lower, "cm", "厘米", "毫米", "mm")) {
            java.util.regex.Matcher m = java.util.regex.Pattern
                    .compile("(\\d+\\s*[×xX*\\-]?\\s*\\d+(?:\\s*[×xX*\\-]?\\s*\\d+)?\\s*(?:cm|厘米|mm|毫米)?)")
                    .matcher(text);
            if (m.find()) return m.group(1).trim();
        }
        if (containsAny(lower, "小号", "迷你")) return "迷你款（约 8-10cm）";
        if (containsAny(lower, "大号", "超大")) return "大号（约 20-25cm）";
        return switch (category) {
            case "钩织" -> "高约 15cm（可定制）";
            case "滴胶" -> "直径 4-6cm 吊坠";
            case "穿戴甲" -> "标准甲型 S/M";
            case "粘土" -> "高约 10-12cm";
            case "缠花" -> "发簪长约 18cm";
            case "拼豆" -> "15×15 格（约 12cm）";
            default -> "按沟通确认尺寸";
        };
    }

    private static int[] suggestBudget(String category, String text) {
        String lower = text.toLowerCase();
        int min = 80, max = 200;
        if (containsAny(lower, "婚礼", "高级", "复杂", "精细")) { min = 200; max = 500; }
        else if (containsAny(lower, "简单", "小", "迷你")) { min = 50; max = 120; }
        else switch (category) {
            case "钩织" -> { min = 80; max = 180; }
            case "滴胶" -> { min = 60; max = 150; }
            case "穿戴甲" -> { min = 99; max = 199; }
            case "粘土" -> { min = 68; max = 158; }
            case "缠花" -> { min = 120; max = 280; }
            case "拼豆" -> { min = 39; max = 99; }
            default -> { min = 80; max = 200; }
        }
        return new int[]{min, max};
    }

    private static int suggestLeadDays(String category, String text) {
        String lower = text.toLowerCase();
        if (containsAny(lower, "急", "尽快", "加急")) return 7;
        if (containsAny(lower, "婚礼", "复杂", "大型")) return 21;
        return switch (category) {
            case "钩织", "缠花" -> 14;
            case "滴胶", "拼豆" -> 10;
            case "穿戴甲" -> 7;
            default -> 14;
        };
    }

    private static String buildRequestTitle(String brief, String category) {
        String core = brief.length() > 18 ? brief.substring(0, 18) + "…" : brief;
        return "【定制】" + category + " · " + core;
    }

    private static String buildStructuredDescription(String brief, String category, String size,
                                                     String material, String imagePrompt) {
        StringBuilder sb = new StringBuilder();
        sb.append("【需求概述】").append(brief.trim()).append("\n\n");
        sb.append("【作品分类】").append(category).append("\n");
        sb.append("【目标尺寸】").append(size).append("\n");
        sb.append("【材质偏好】").append(material).append("\n");
        if (imagePrompt != null && !imagePrompt.isBlank()) {
            sb.append("【参考灵感】").append(imagePrompt.trim()).append("\n");
        }
        sb.append("\n【交付说明】希望手作人接单后先确认细节，支持小幅修改。");
        return sb.toString();
    }

    private static String detectProductCategory(String keywords) {
        String lower = keywords.toLowerCase();
        if (containsAny(lower, "钩织", "钩针", "毛线")) return "crochet";
        if (containsAny(lower, "滴胶", "干花", "树脂")) return "resin";
        if (containsAny(lower, "穿戴甲", "美甲")) return "nails";
        if (containsAny(lower, "粘土", "软陶")) return "clay";
        if (containsAny(lower, "缠花", "团扇", "发簪")) return "flower";
        if (containsAny(lower, "拼豆")) return "perler";
        if (containsAny(lower, "刺绣", "绣")) return "embroidery";
        if (containsAny(lower, "串珠", "手链")) return "bead";
        if (containsAny(lower, "皮", "皮革")) return "leather";
        if (containsAny(lower, "木", "雕刻")) return "wood";
        if (containsAny(lower, "香薰", "蜡烛")) return "candle";
        if (containsAny(lower, "纸艺", "衍纸")) return "paper";
        return "crochet";
    }

    private static String suggestCraftTechnique(String category, String keywords) {
        return switch (category) {
            case "crochet" -> "钩针编织";
            case "resin" -> "UV滴胶";
            case "nails" -> "手工穿戴甲";
            case "clay" -> "软陶塑形";
            case "flower" -> "缠花";
            case "perler" -> "拼豆";
            case "embroidery" -> "刺绣";
            case "bead" -> "串珠";
            case "leather" -> "皮雕";
            case "wood" -> "木雕";
            case "candle" -> "香薰蜡烛";
            case "paper" -> "衍纸";
            default -> "";
        };
    }

    private static int suggestPrice(String category, String keywords) {
        String lower = keywords.toLowerCase();
        int base = switch (category) {
            case "crochet" -> 128;
            case "resin" -> 88;
            case "nails" -> 99;
            case "clay" -> 78;
            case "flower" -> 168;
            case "perler" -> 58;
            case "embroidery" -> 138;
            case "bead" -> 98;
            case "leather" -> 198;
            case "wood" -> 228;
            case "candle" -> 68;
            case "paper" -> 88;
            default -> 99;
        };
        if (containsAny(lower, "复杂", "高级", "定制", "婚礼")) base = (int) (base * 1.4);
        if (containsAny(lower, "简单", "迷你", "小")) base = (int) (base * 0.75);
        return Math.max(29, (base / 10) * 10 - 1);
    }

    private static String detectSkillCategory(String text) {
        String lower = text.toLowerCase();
        if (containsAny(lower, "摄影", "拍照", "修图", "人像")) return "摄影";
        if (containsAny(lower, "设计", "排版", "logo", "海报", "ui")) return "设计";
        if (containsAny(lower, "钩织", "钩针", "毛线", "编织")) return "钩织";
        if (containsAny(lower, "滴胶", "干花", "树脂")) return "滴胶";
        if (containsAny(lower, "缠花", "团扇", "发簪")) return "缠花";
        return "钩织";
    }

    private static String suggestSkillDuration(String category, String text) {
        String lower = text.toLowerCase();
        if (containsAny(lower, "入门", "基础", "零基础")) {
            return switch (category) {
                case "摄影" -> "1.5小时";
                case "设计" -> "2小时";
                default -> "2小时";
            };
        }
        if (containsAny(lower, "进阶", "高级", "完整")) {
            return switch (category) {
                case "摄影" -> "3小时";
                case "设计" -> "3-4小时";
                default -> "3小时";
            };
        }
        return switch (category) {
            case "摄影" -> "2小时";
            case "设计" -> "2.5小时";
            case "滴胶" -> "1.5小时";
            default -> "2小时";
        };
    }

    private static int suggestSkillCost(String category, String text) {
        String lower = text.toLowerCase();
        int base = switch (category) {
            case "摄影" -> 18;
            case "设计" -> 22;
            case "钩织" -> 12;
            case "滴胶" -> 10;
            case "缠花" -> 15;
            default -> 10;
        };
        if (containsAny(lower, "进阶", "高级", "一对一", "定制")) base += 8;
        if (containsAny(lower, "入门", "体验", "基础")) base = Math.max(8, base - 3);
        return Math.min(100, Math.max(5, base));
    }

    private static String buildSkillTitle(String keywords, String category) {
        String core = keywords.length() > 16 ? keywords.substring(0, 16) + "…" : keywords;
        return switch (category) {
            case "摄影" -> "一对一摄影指导 · " + core;
            case "设计" -> "设计技能分享 · " + core;
            default -> "零基础教你 · " + core;
        };
    }

    private static String buildSkillDescription(String keywords, String category, String duration) {
        return "我可以提供【" + category + "】相关的技能交换服务，主题围绕「" + keywords.trim() + "」。"
                + "服务时长约 " + duration + "，支持线上/线下沟通（具体可协商）。"
                + "适合想入门或完成一个小作品的朋友，我会按步骤讲解并答疑。"
                + "交换完成后请双方互评，共同维护社区信用。";
    }
}
