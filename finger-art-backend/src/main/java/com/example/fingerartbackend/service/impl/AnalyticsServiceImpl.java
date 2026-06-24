package com.example.fingerartbackend.service.impl;

import com.example.fingerartbackend.entity.CustomOrder;
import com.example.fingerartbackend.entity.CustomRequest;
import com.example.fingerartbackend.entity.Product;
import com.example.fingerartbackend.entity.User;
import com.example.fingerartbackend.mapper.*;
import com.example.fingerartbackend.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据分析服务实现类。
 */
@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private SkillMapper skillMapper;
    @Autowired
    private CustomOrderMapper orderMapper;
    @Autowired
    private CustomRequestMapper requestMapper;
    @Autowired
    private FollowMapper followMapper;
    @Autowired
    private ContentReportMapper reportMapper;

    /**
     * 查询数据分析信息。
     */
    @Override
    public Map<String, Object> getPlatformAnalytics() {
        Map<String, Object> data = new HashMap<>();

        List<Product> products = productMapper.findAll().stream()
                .filter(p -> "APPROVED".equals(p.getStatus()))
                .collect(Collectors.toList());
        List<CustomRequest> openRequests = requestMapper.findAll().stream()
                .filter(r -> "OPEN".equals(r.getStatus()))
                .collect(Collectors.toList());

        data.put("categoryTrend", countByField(products, Product::getCategory));
        data.put("craftTrend", countByField(products.stream()
                .filter(p -> p.getCraftTechnique() != null && !p.getCraftTechnique().isBlank())
                .collect(Collectors.toList()), Product::getCraftTechnique));
        data.put("demandByCategory", countByField(openRequests, CustomRequest::getCategory));
        data.put("supplyByCategory", countByField(products, Product::getCategory));

        Map<String, Long> orderStatus = orderMapper.findAll().stream()
                .collect(Collectors.groupingBy(o -> o.getStatus() != null ? o.getStatus() : "UNKNOWN", Collectors.counting()));
        data.put("orderStatusDistribution", orderStatus);

        double coinCirculation = userMapper.findAll().stream()
                .mapToDouble(u -> u.getZaowuBiBalance() != null ? u.getZaowuBiBalance() : 0)
                .sum();
        data.put("coinCirculation", Math.round(coinCirculation * 100.0) / 100.0);
        data.put("pendingReports", reportMapper.countByStatus("PENDING"));
        data.put("pendingProducts", productMapper.findAll().stream().filter(p -> "PENDING".equals(p.getStatus())).count());
        data.put("pendingSkills", skillMapper.findAll().stream().filter(s -> "PENDING".equals(s.getStatus())).count());
        data.put("openRequests", openRequests.size());
        data.put("artisanCount", userMapper.findAll().stream().filter(u -> "ARTISAN".equals(u.getRole())).count());

        return data;
    }

    /**
     * 查询数据分析信息。
     */
    @Override
    public Map<String, Object> getArtisanAnalytics(Long userId) {
        User user = userMapper.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));

        List<Product> myProducts = productMapper.findByCreatorId(userId);
        List<CustomOrder> artisanOrders = orderMapper.findByArtisanId(userId);
        List<CustomOrder> buyerOrders = orderMapper.findByBuyerId(userId);

        long completedAsArtisan = artisanOrders.stream().filter(o -> "COMPLETED".equals(o.getStatus())).count();
        double revenue = artisanOrders.stream()
                .filter(o -> "COMPLETED".equals(o.getStatus()))
                .mapToDouble(o -> o.getPrice() != null ? o.getPrice() : 0)
                .sum();

        Map<String, Object> data = new HashMap<>();
        data.put("username", user.getUsername());
        data.put("coinBalance", user.getZaowuBiBalance());
        data.put("creditScore", user.getCreditScore());
        data.put("rating", user.getRating());
        data.put("productCount", myProducts.size());
        data.put("approvedProductCount", myProducts.stream().filter(p -> "APPROVED".equals(p.getStatus())).count());
        data.put("totalLikes", myProducts.stream().mapToInt(p -> p.getLikes() != null ? p.getLikes() : 0).sum());
        data.put("followerCount", followMapper.findByFollowingId(userId).size());
        data.put("followingCount", followMapper.findByFollowerId(userId).size());
        data.put("artisanOrderCount", artisanOrders.size());
        data.put("buyerOrderCount", buyerOrders.size());
        data.put("completedOrderCount", completedAsArtisan);
        data.put("revenue", Math.round(revenue * 100.0) / 100.0);
        data.put("ordersByStatus", artisanOrders.stream()
                .collect(Collectors.groupingBy(o -> o.getStatus() != null ? o.getStatus() : "UNKNOWN", Collectors.counting())));
        data.put("topProducts", myProducts.stream()
                .sorted(Comparator.comparingInt((Product p) -> p.getLikes() != null ? p.getLikes() : 0).reversed())
                .limit(3)
                .map(p -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", p.getId());
                    m.put("title", p.getTitle());
                    m.put("image", p.getImage());
                    m.put("price", p.getPrice());
                    m.put("likes", p.getLikes() != null ? p.getLikes() : 0);
                    m.put("status", p.getStatus());
                    m.put("exposureBoost", p.getExposureBoost());
                    return m;
                })
                .collect(Collectors.toList()));

        return data;
    }

    /**
     * 执行 countByField 相关逻辑。
     */
    private <T> List<Map<String, Object>> countByField(List<T> items, java.util.function.Function<T, String> getter) {
        Map<String, Long> counts = items.stream()
                .map(getter)
                .filter(Objects::nonNull)
                .filter(s -> !s.isBlank())
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));

        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(e -> {
                    Map<String, Object> row = new HashMap<>();
                    row.put("name", e.getKey());
                    row.put("value", e.getValue());
                    return row;
                })
                .collect(Collectors.toList());
    }
}
