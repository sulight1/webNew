package com.example.fingerartbackend.service.impl;

import com.example.fingerartbackend.config.AiChatProperties;
import com.example.fingerartbackend.config.AiImageProperties;
import com.example.fingerartbackend.entity.CustomOrder;
import com.example.fingerartbackend.entity.Product;
import com.example.fingerartbackend.service.AiChatService;
import com.example.fingerartbackend.service.OrderService;
import com.example.fingerartbackend.service.ProductService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AiChatServiceImpl implements AiChatService {

    private static final String DEFAULT_PRODUCT_IMAGE =
            "https://images.unsplash.com/photo-1620799140408-edc6dcb6d633?w=400";

    private static final Map<String, String> ORDER_STATUS_LABELS = Map.ofEntries(
            Map.entry("PENDING_CONFIRM", "待手作人确认"),
            Map.entry("PENDING_PAY", "待支付定金"),
            Map.entry("PRODUCING", "制作中"),
            Map.entry("HALF_FINISHED_CONFIRM", "半成品待确认"),
            Map.entry("PENDING_SHIP", "待发货"),
            Map.entry("PENDING_ACCEPT", "待收货"),
            Map.entry("PENDING_BALANCE", "待付尾款"),
            Map.entry("COMPLETED", "已完成"),
            Map.entry("DISPUTED", "纠纷处理中"),
            Map.entry("CANCELLED", "已取消")
    );

    @Autowired
    private AiChatProperties chatProperties;

    @Autowired
    private AiImageProperties imageProperties;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Map<String, Object> chat(List<Map<String, String>> messages, Long userId, String pageContext) {
        if (messages == null || messages.isEmpty()) {
            throw new IllegalArgumentException("消息不能为空");
        }

        String lastMessage = messages.get(messages.size() - 1).get("content");
        if (lastMessage == null || lastMessage.isBlank()) {
            throw new IllegalArgumentException("消息内容不能为空");
        }

        boolean orderQuery = isOrderQuery(lastMessage);
        boolean productQuery = isProductQuery(lastMessage);

        String orderContext = null;
        if (orderQuery && userId != null) {
            orderContext = buildOrderContext(userId);
        } else if (orderQuery) {
            orderContext = "用户未登录，无法查询具体订单。请引导用户登录后在「我的订单」查看，或说明手作一般制作周期 3-7 天。";
        }

        List<Map<String, Object>> recommendations = null;
        if (productQuery) {
            List<Product> products = searchProductsForMessage(lastMessage, 5);
            recommendations = toRecommendationList(products);
        }

        List<Map<String, String>> actions = buildActions(lastMessage, orderQuery, productQuery, userId);

        String reply;
        String source;
        try {
            if (chatProperties.isEnabled() && hasApiKey()) {
                String systemPrompt = buildSystemPrompt(orderContext, recommendations, pageContext);
                reply = callQwen(systemPrompt, messages);
                source = "qwen";
            } else {
                reply = buildFallbackReply(lastMessage, orderContext, recommendations);
                source = "smart-fallback";
            }
        } catch (Exception e) {
            System.err.println("AI 对话调用失败，使用智能兜底: " + e.getMessage());
            reply = buildFallbackReply(lastMessage, orderContext, recommendations);
            source = "smart-fallback";
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("reply", reply);
        data.put("source", source);
        if (recommendations != null && !recommendations.isEmpty()) {
            data.put("recommendations", recommendations);
        }
        if (!actions.isEmpty()) {
            data.put("actions", actions);
        }
        return data;
    }

    @Override
    public Map<String, Object> recommend(String query, int limit) {
        int cap = limit > 0 ? Math.min(limit, 10) : 5;
        List<Product> products = searchProductsForMessage(query != null ? query : "", cap);
        List<Map<String, Object>> items = new ArrayList<>();
        List<String> keywords = extractSearchKeywords(query != null ? query : "");

        for (Product p : products) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", p.getId());
            item.put("title", p.getTitle());
            item.put("price", p.getPrice() != null ? p.getPrice() : 0);
            item.put("image", resolveImage(p));
            item.put("reason", buildRecommendReason(p, keywords));
            items.add(item);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("keywords", keywords);
        data.put("products", items);
        return data;
    }

    private boolean hasApiKey() {
        String key = resolveApiKey();
        return key != null && !key.isBlank();
    }

    private String resolveApiKey() {
        if (chatProperties.getApiKey() != null && !chatProperties.getApiKey().isBlank()) {
            return chatProperties.getApiKey();
        }
        return imageProperties.getDashscope().getApiKey();
    }

    private String callQwen(String systemPrompt, List<Map<String, String>> messages) throws Exception {
        String url = chatProperties.getBaseUrl() + "/compatible-mode/v1/chat/completions";

        List<Map<String, String>> apiMessages = new ArrayList<>();
        apiMessages.add(Map.of("role", "system", "content", systemPrompt));
        for (Map<String, String> msg : messages) {
            String role = msg.get("role");
            String content = msg.get("content");
            if (role == null || content == null || content.isBlank()) continue;
            if ("assistant".equals(role) || "user".equals(role)) {
                apiMessages.add(Map.of("role", role, "content", content));
            }
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", chatProperties.getModel());
        body.put("messages", apiMessages);
        body.put("max_tokens", chatProperties.getMaxTokens());
        body.put("temperature", chatProperties.getTemperature());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(resolveApiKey());

        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("通义千问请求失败：" + response.getStatusCode());
        }

        JsonNode root = objectMapper.readTree(response.getBody());
        if (root.has("error")) {
            throw new RuntimeException("通义千问错误：" + root.path("error").path("message").asText("未知错误"));
        }
        String content = root.path("choices").path(0).path("message").path("content").asText(null);
        if (content == null || content.isBlank()) {
            throw new RuntimeException("通义千问返回空内容");
        }
        return content.trim();
    }

    private String buildSystemPrompt(String orderContext, List<Map<String, Object>> recommendations,
                                     String pageContext) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是「指尖造物」平台的 AI 造物管家，名叫小造。你温暖、专业、简洁，擅长手作文创、定制订单和技能交换。\n\n");
        sb.append("【平台能力】\n");
        sb.append("- 造物市集：浏览/购买手作作品\n");
        sb.append("- 需求大厅：发布定制需求，手作人接单\n");
        sb.append("- 技能交换：用技能互换（摄影、设计、钩织等）\n");
        sb.append("- 造物币：发布作品、完成订单、签到等可获得，可打赏和兑换\n");
        sb.append("- 手作达人/手作人：ARTISAN 角色可上架作品\n\n");
        sb.append("【回答规则】\n");
        sb.append("1. 用中文回复，语气亲切但不啰嗦，控制在 150 字以内（除非用户问订单详情）\n");
        sb.append("2. 不要编造具体商品价格、库存、物流单号\n");
        sb.append("3. 若下方提供了「真实订单数据」或「推荐商品」，必须基于这些数据回答，可引用标题和状态\n");
        sb.append("4. 推荐商品时引导用户点击下方卡片查看详情\n");
        sb.append("5. 用户问定制 → 引导去「需求大厅」；问技能 → 引导「技能交换」；问订单未登录 → 引导登录\n");
        sb.append("6. 不要使用过多 emoji，最多 1 个\n\n");

        if (pageContext != null && !pageContext.isBlank()) {
            sb.append("【用户当前页面】").append(pageContext).append("\n\n");
        }
        if (orderContext != null && !orderContext.isBlank()) {
            sb.append("【真实订单数据】\n").append(orderContext).append("\n\n");
        }
        if (recommendations != null && !recommendations.isEmpty()) {
            sb.append("【推荐商品（已在界面展示卡片，回复中可提及）】\n");
            for (Map<String, Object> rec : recommendations) {
                sb.append("- ").append(rec.get("title"))
                        .append("，￥").append(rec.get("price")).append("\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private String buildOrderContext(Long userId) {
        List<CustomOrder> orders = orderService.getBuyerOrders(userId);
        if (orders == null || orders.isEmpty()) {
            return "该用户目前没有任何订单。";
        }

        List<CustomOrder> active = orders.stream()
                .filter(o -> !"COMPLETED".equals(o.getStatus()) && !"CANCELLED".equals(o.getStatus()))
                .sorted(Comparator.comparing(CustomOrder::getCreateTime,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .collect(Collectors.toList());

        List<CustomOrder> toShow = active.isEmpty()
                ? orders.stream()
                .sorted(Comparator.comparing(CustomOrder::getCreateTime,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(3)
                .collect(Collectors.toList())
                : active;

        StringBuilder sb = new StringBuilder();
        sb.append("共 ").append(orders.size()).append(" 笔订单");
        if (!active.isEmpty()) {
            sb.append("，其中 ").append(active.size()).append(" 笔进行中");
        }
        sb.append("：\n");

        for (CustomOrder o : toShow) {
            sb.append("- 订单#").append(o.getId())
                    .append("《").append(o.getProductTitle() != null ? o.getProductTitle() : "定制作品").append("》")
                    .append("，状态：").append(formatOrderStatus(o.getStatus()));
            if (o.getArtisanName() != null) {
                sb.append("，手作人：").append(o.getArtisanName());
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private String formatOrderStatus(String status) {
        if (status == null) return "未知";
        return ORDER_STATUS_LABELS.getOrDefault(status, status);
    }

    private List<Product> searchProductsForMessage(String message, int limit) {
        LinkedHashSet<Long> seen = new LinkedHashSet<>();
        List<Product> result = new ArrayList<>();

        for (String kw : extractSearchKeywords(message)) {
            for (Product p : productService.searchApprovedProducts(kw, limit, null)) {
                if (seen.add(p.getId())) {
                    result.add(p);
                    if (result.size() >= limit) return result;
                }
            }
        }

        if (result.isEmpty()) {
            for (Product p : productService.getHotProducts(limit, null)) {
                if (seen.add(p.getId())) {
                    result.add(p);
                }
            }
        }
        return result;
    }

    private List<String> extractSearchKeywords(String message) {
        LinkedHashSet<String> keywords = new LinkedHashSet<>();
        String lower = message.toLowerCase();

        addSynonymKeywords(keywords, lower);

        for (String token : message.split("[\\s,，、。！？!?；;：:]+")) {
            String t = token.trim();
            if (t.length() >= 2 && !isStopWord(t)) {
                keywords.add(t);
            }
        }

        if (keywords.isEmpty() && message.trim().length() >= 2) {
            keywords.add(message.trim());
        }
        return new ArrayList<>(keywords);
    }

    private void addSynonymKeywords(Set<String> keywords, String lower) {
        if (containsAny(lower, "汉服", "头饰", "发簪", "发饰", "新中式", "国风", "古风", "东方")) {
            keywords.add("缠花");
            keywords.add("发簪");
            keywords.add("头饰");
        }
        if (containsAny(lower, "耳环", "耳饰", "音乐节", "夸张", "辣妹", "y2k", "千禧")) {
            keywords.add("耳环");
            keywords.add("耳饰");
        }
        if (containsAny(lower, "礼物", "闺蜜", "送人", "生日")) {
            keywords.add("钩织");
            keywords.add("穿戴甲");
            keywords.add("香薰");
            keywords.add("定制");
        }
        if (containsAny(lower, "钩织", "毛线", "编织")) keywords.add("钩织");
        if (containsAny(lower, "滴胶", "干花", "树脂")) keywords.add("滴胶");
        if (containsAny(lower, "穿戴甲", "美甲")) keywords.add("穿戴甲");
        if (containsAny(lower, "粘土", "软陶")) keywords.add("粘土");
        if (containsAny(lower, "拼豆")) keywords.add("拼豆");
        if (containsAny(lower, "串珠", "手链")) keywords.add("串珠");
        if (containsAny(lower, "团扇")) keywords.add("团扇");
    }

    private boolean isStopWord(String word) {
        return containsAny(word.toLowerCase(),
                "我想", "想要", "帮我", "推荐", "有没有", "什么", "怎么", "可以", "适合", "请问", "一下", "吗", "呢", "的");
    }

    private List<Map<String, Object>> toRecommendationList(List<Product> products) {
        if (products == null || products.isEmpty()) return List.of();
        return products.stream().map(p -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", p.getId());
            m.put("title", p.getTitle());
            m.put("price", p.getPrice() != null ? p.getPrice() : 0);
            m.put("image", resolveImage(p));
            return m;
        }).collect(Collectors.toList());
    }

    private String resolveImage(Product p) {
        if (p.getImage() != null && !p.getImage().isBlank()) {
            return p.getImage();
        }
        return DEFAULT_PRODUCT_IMAGE;
    }

    private String buildRecommendReason(Product p, List<String> keywords) {
        for (String kw : keywords) {
            if (p.getTitle() != null && p.getTitle().toLowerCase().contains(kw.toLowerCase())) {
                return "标题匹配「" + kw + "」";
            }
            if (p.getCraftTechnique() != null && p.getCraftTechnique().toLowerCase().contains(kw.toLowerCase())) {
                return "工艺匹配「" + kw + "」";
            }
        }
        if (p.getLikes() != null && p.getLikes() > 0) {
            return "热门作品，获赞 " + p.getLikes();
        }
        return "为你精选的手作好物";
    }

    private List<Map<String, String>> buildActions(String message, boolean orderQuery,
                                                   boolean productQuery, Long userId) {
        List<Map<String, String>> actions = new ArrayList<>();
        String lower = message.toLowerCase();

        if (orderQuery) {
            if (userId == null) {
                actions.add(action("去登录", "/login"));
            } else {
                actions.add(action("查看我的订单", "/account?menu=buyer-orders"));
            }
        }
        if (productQuery) {
            actions.add(action("逛造物市集", "/marketplace"));
        }
        if (containsAny(lower, "定制", "定做", "需求")) {
            actions.add(action("发布定制需求", "/custom-request-pool"));
        }
        if (containsAny(lower, "技能", "交换", "学习", "教程")) {
            actions.add(action("技能交换", "/skill-exchange"));
        }
        if (containsAny(lower, "造物币", "签到", "金币")) {
            actions.add(action("个人中心", "/account"));
        }
        return actions;
    }

    private Map<String, String> action(String label, String path) {
        return Map.of("label", label, "path", path);
    }

    private String buildFallbackReply(String message, String orderContext,
                                      List<Map<String, Object>> recommendations) {
        String lower = message.toLowerCase();

        if (orderContext != null) {
            if (orderContext.contains("没有任何订单")) {
                return "你目前还没有订单哦～去造物市集逛逛，看到喜欢的可以直接下单或发起定制！";
            }
            if (orderContext.contains("未登录")) {
                return "查询订单需要先登录～登录后我可以告诉你每笔订单的实时进度！";
            }
            return "帮你查到了最近的订单情况：\n" + orderContext.trim() + "\n如需详情可点击「查看我的订单」。";
        }

        if (recommendations != null && !recommendations.isEmpty()) {
            String names = recommendations.stream()
                    .limit(3)
                    .map(r -> "《" + r.get("title") + "》")
                    .collect(Collectors.joining("、"));
            return "为你找到了这些作品：" + names + "。点击下方卡片可查看详情，告诉我预算或风格还能帮你缩小范围～";
        }

        if (containsAny(lower, "定制", "定做")) {
            return "可以定制！去「需求大厅」描述你想要的款式、尺寸和预算，手作人会来接单。也可以先和作者私信沟通细节。";
        }
        if (containsAny(lower, "造物币", "金币")) {
            return "造物币是平台专属货币～发布作品、完成订单、每日签到都能获得，可用来打赏作品或参与技能交换。";
        }
        if (containsAny(lower, "技能", "交换")) {
            return "技能交换是社区特色！你会摄影、设计或某种手作，都可以和别人互换技能。去「技能交换」板块发布或浏览吧～";
        }
        if (containsAny(lower, "入门", "新手", "教程")) {
            return "新手建议从串珠、钩织或超轻粘土入门，材料包便宜、成就感来得快。社区里也有很多手作人分享经验！";
        }
        if (containsAny(lower, "你好", "嗨", "在吗")) {
            return "在的～我是 AI 造物管家小造，可以帮你找作品、查订单、了解定制和造物币。说说你想找什么吧！";
        }

        return "收到！你可以告诉我风格（如新中式、Y2K）、预算或使用场景，我来帮你推荐；也可以问订单进度、定制流程或造物币规则。";
    }

    private boolean isOrderQuery(String message) {
        String lower = message.toLowerCase();
        return containsAny(lower, "订单", "发货", "物流", "进度", "做到哪", "什么时候", "到哪了", "快递");
    }

    private boolean isProductQuery(String message) {
        String lower = message.toLowerCase();
        if (isOrderQuery(message)) return false;
        return containsAny(lower,
                "推荐", "买", "找", "想要", "礼物", "闺蜜", "汉服", "头饰", "发簪", "耳环",
                "饰品", "新中式", "国风", "古风", "钩织", "滴胶", "穿戴甲", "粘土", "拼豆",
                "串珠", "市集", "作品", "商品", "适合", "搭配", "音乐节", "辣妹", "y2k");
    }

    private boolean containsAny(String text, String... keywords) {
        for (String kw : keywords) {
            if (text.contains(kw)) return true;
        }
        return false;
    }
}
