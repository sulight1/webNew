package com.example.fingerartbackend.service.impl;

import com.example.fingerartbackend.auth.AuthContext;
import com.example.fingerartbackend.config.Kuaidi100Properties;
import com.example.fingerartbackend.dto.LogisticsTraceResult;
import com.example.fingerartbackend.dto.LogisticsTrackItem;
import com.example.fingerartbackend.entity.CustomOrder;
import com.example.fingerartbackend.service.LogisticsService;
import com.example.fingerartbackend.service.OrderService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/**
 * 物流服务实现类。
 */
@Service
public class LogisticsServiceImpl implements LogisticsService {

    private static final Map<String, String> COMPANY_CODES = Map.ofEntries(
            Map.entry("顺丰", "shunfeng"),
            Map.entry("顺丰速运", "shunfeng"),
            Map.entry("中通", "zhongtong"),
            Map.entry("中通快递", "zhongtong"),
            Map.entry("圆通", "yuantong"),
            Map.entry("圆通速递", "yuantong"),
            Map.entry("韵达", "yunda"),
            Map.entry("韵达快递", "yunda"),
            Map.entry("申通", "shentong"),
            Map.entry("申通快递", "shentong"),
            Map.entry("邮政", "ems"),
            Map.entry("邮政EMS", "ems"),
            Map.entry("EMS", "ems"),
            Map.entry("京东", "jd"),
            Map.entry("京东物流", "jd"),
            Map.entry("极兔", "jtexpress"),
            Map.entry("极兔速递", "jtexpress"),
            Map.entry("德邦", "debangwuliu"),
            Map.entry("德邦快递", "debangwuliu")
    );

    private static final Map<String, String> STATE_LABELS = Map.of(
            "0", "在途",
            "1", "揽收",
            "2", "疑难",
            "3", "签收",
            "4", "退签",
            "5", "派件",
            "6", "退回"
    );

    @Autowired
    private OrderService orderService;

    @Autowired
    private Kuaidi100Properties kuaidi100Properties;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 执行 queryOrderLogistics 相关逻辑。
     */
    @Override
    public LogisticsTraceResult queryOrderLogistics(Long orderId, Long userId) {
        CustomOrder order = orderService.getOrder(orderId);
        assertCanView(order, userId);

        if (order.getTrackingNumber() == null || order.getTrackingNumber().isBlank()) {
            throw new RuntimeException("该订单尚未填写物流单号");
        }

        String company = order.getShippingCompany() != null ? order.getShippingCompany().trim() : "";
        String trackingNo = order.getTrackingNumber().trim();
        String companyCode = resolveCompanyCode(company);

        LogisticsTraceResult result = new LogisticsTraceResult();
        result.setShippingCompany(company);
        result.setTrackingNumber(trackingNo);
        result.setCompanyCode(companyCode);
        result.setFallbackUrl(buildFallbackUrl(companyCode, trackingNo));
        result.setHint("可复制单号，或在快递100 / 微信物流助手查询");

        if (kuaidi100Properties.isEnabled()
                && kuaidi100Properties.getCustomer() != null && !kuaidi100Properties.getCustomer().isBlank()
                && kuaidi100Properties.getKey() != null && !kuaidi100Properties.getKey().isBlank()
                && companyCode != null) {
            try {
                fillFromKuaidi100(result, companyCode, trackingNo);
                result.setApiAvailable(true);
                result.setHint("平台已同步最新物流轨迹；也可复制单号到快递公司 App 查询");
                return result;
            } catch (Exception e) {
                result.setApiAvailable(false);
                result.setHint("实时查询暂不可用：" + e.getMessage() + "。请复制单号或使用下方链接查询");
                return result;
            }
        }

        result.setApiAvailable(false);
        return result;
    }

    /**
     * 断言业务条件，不满足则抛异常。
     */
    private void assertCanView(CustomOrder order, Long userId) {
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }
        if (AuthContext.isAdmin()) {
            return;
        }
        if (!Objects.equals(order.getBuyerId(), userId) && !Objects.equals(order.getArtisanId(), userId)) {
            throw new RuntimeException("无权查看该订单物流");
        }
    }

    /**
     * 执行 fillFromKuaidi100 相关逻辑。
     */
    private void fillFromKuaidi100(LogisticsTraceResult result, String companyCode, String trackingNo) throws Exception {
        Map<String, String> paramMap = new LinkedHashMap<>();
        paramMap.put("com", companyCode);
        paramMap.put("num", trackingNo);
        String param = objectMapper.writeValueAsString(paramMap);

        String customer = kuaidi100Properties.getCustomer().trim();
        String key = kuaidi100Properties.getKey().trim();
        String sign = md5Upper(param + key + customer);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("customer", customer);
        body.add("sign", sign);
        body.add("param", param);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        ResponseEntity<String> response = restTemplate.postForEntity(
                kuaidi100Properties.getQueryUrl(),
                new HttpEntity<>(body, headers),
                String.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("快递100接口无响应");
        }

        JsonNode root = objectMapper.readTree(response.getBody());
        if (!"ok".equalsIgnoreCase(root.path("message").asText())
                && root.path("status").asInt(0) != 200) {
            String msg = root.path("message").asText("查询失败");
            throw new RuntimeException(msg);
        }

        String state = root.path("state").asText("");
        result.setStatusText(STATE_LABELS.getOrDefault(state, "运输中"));

        List<LogisticsTrackItem> tracks = new ArrayList<>();
        JsonNode data = root.path("data");
        if (data.isArray()) {
            for (JsonNode node : data) {
                LogisticsTrackItem item = new LogisticsTrackItem();
                item.setTime(node.path("time").asText(node.path("ftime").asText("")));
                item.setContext(node.path("context").asText(""));
                tracks.add(item);
            }
        }
        result.setTracks(tracks);
    }

    /**
     * 执行 resolveCompanyCode 相关逻辑。
     */
    private String resolveCompanyCode(String companyName) {
        if (companyName == null || companyName.isBlank()) {
            return null;
        }
        String trimmed = companyName.trim();
        if (COMPANY_CODES.containsKey(trimmed)) {
            return COMPANY_CODES.get(trimmed);
        }
        for (Map.Entry<String, String> entry : COMPANY_CODES.entrySet()) {
            if (trimmed.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * 构建响应对象。
     */
    private String buildFallbackUrl(String companyCode, String trackingNo) {
        try {
            String encodedNu = URLEncoder.encode(trackingNo, StandardCharsets.UTF_8);
            if (companyCode != null && !companyCode.isBlank()) {
                return "https://www.kuaidi100.com/chaxun?com=" + companyCode + "&nu=" + encodedNu;
            }
            return "https://www.kuaidi100.com/chaxun?nu=" + encodedNu;
        } catch (Exception e) {
            return "https://www.kuaidi100.com/";
        }
    }

    /**
     * 执行 md5Upper 相关逻辑。
     */
    private String md5Upper(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString().toUpperCase();
    }
}
